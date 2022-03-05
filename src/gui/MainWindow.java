package gui;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

import dao.DBUtil;
import enums.SchemaType;
import pojos.Relationship;
import util.FileSaver;
import util.SQLToJSONConverter;

public class MainWindow extends JFrame {
	private static final long serialVersionUID = -2278436951424873713L;
	private static final Font FONT = new Font("Calibri Light", Font.PLAIN, 15);
	private static String dbName;
	private JComboBox<String> databasesComboBox;
	private JList<Relationship> listRelationships;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MainWindow();
			}
		});
	}

	public MainWindow() {
		initialize();
	}

	private void initialize() {
		setBounds(100, 100, 465, 430);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("SQL to NoSQL tool");
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
		getContentPane().setLayout(null);

		JLabel lblDatabases = new JLabel("Selectionner une base de donn\u00E9es");
		lblDatabases.setFont(FONT);
		lblDatabases.setBounds(10, 20, 220, 40);
		getContentPane().add(lblDatabases);

		databasesComboBox = new JComboBox<>();
		databasesComboBox.setFont(FONT);
		databasesComboBox.setBounds(242, 20, 196, 40);
		getContentPane().add(databasesComboBox);
		populateDatabasesComboBox();
		databasesComboBox.addActionListener(databasesComboBoxActionListener());

		JButton btnSubmit = new JButton("G\u00E9n\u00E9rer les fichiers JSON");
		btnSubmit.setFont(FONT);
		btnSubmit.setBounds(10, 340, 196, 40);
		getContentPane().add(btnSubmit);
		btnSubmit.addActionListener(btnSubmitActionListener());

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 96, 428, 232);
		getContentPane().add(scrollPane);

		listRelationships = new JList<>();
		scrollPane.setViewportView(listRelationships);

		JLabel lbl = new JLabel("Les relations entre les tables :");
		lbl.setFont(FONT);
		lbl.setBounds(12, 70, 175, 27);
		getContentPane().add(lbl);
		
		JButton btnSQLToJSON = new JButton("G\u00E9n\u00E9rer JSON depuis SQL");
		btnSQLToJSON.setFont(FONT);
		btnSQLToJSON.setBounds(242, 340, 196, 40);
		getContentPane().add(btnSQLToJSON);
		btnSQLToJSON.addActionListener(sqlTojsonConverter());
	}

	private ActionListener sqlTojsonConverter() {
		return event -> {
			JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
			int response = fileChooser.showSaveDialog(null);
			if( response != JFileChooser.APPROVE_OPTION) 
				return;
			
			try {
				SQLToJSONConverter.generateJSONFilesFromSQLFile( fileChooser.getSelectedFile().getAbsolutePath() );
				JOptionPane.showMessageDialog(getContentPane(),
						"Les Fichiers sont été créés avec succès\ndans json_no_ref et json_with_ref");
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
	}

	private void populateDatabasesComboBox() {
		DBUtil.getDatabasesNames().forEach(name -> databasesComboBox.addItem(name));
	}

	private ActionListener databasesComboBoxActionListener() {
		return event -> {
			dbName = (String) databasesComboBox.getSelectedItem();
			List<Relationship> list = DBUtil.getRelationshipsBetweenTables(dbName);
			listRelationships.setListData(list.toArray(new Relationship[list.size()]));
		};
	}

	private ActionListener btnSubmitActionListener() {
		return event -> {
			if (DBUtil.getRelatedTablesData(dbName).isEmpty()) {
				JOptionPane.showMessageDialog(getContentPane(),
						"Elle n'existe aucune relation entre les tables de la base de donneés sélectionée", "Message",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			try {
				String message = "";
				String location = "";
				final String[] options = new String[] {"Many to One", "One to Many", "Fichiers séparés"};
				int choice = 2;
				if (DBUtil.getSchemaType(dbName) == SchemaType.STAR)
					choice = JOptionPane.showOptionDialog(getContentPane(),
							"Le schéma de la base de données sélectionée est en étoile.\nVeuillez choisir la méthode d'enregistrement ?",
							"Méthode d'enregistrement", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
				switch (choice) {
				case 0:
					location = FileSaver.saveDataAsJSON(dbName, DBUtil.getManyToOneTablesData(dbName));
					message = "Le fichier json sont été créés avec succès dans :\n" + location + dbName + ".json";
					break;
				case 1:
					location = FileSaver.saveDataAsJSON(dbName, DBUtil.getOneToManyTablesData(dbName));
					message = "Le fichier json a été créé  avec succès dans :\n" + location + dbName + ".json";
					break;
				case 2:
					location = FileSaver.saveDataAsJSON(dbName, DBUtil.getRelatedTablesData(dbName));
					message = "Les fichiers json sont été créés avec succès dans le dossier :\n" + location;
					break;
				default:
					break;
				}
				if (!message.isEmpty())
					JOptionPane.showMessageDialog(getContentPane(), message);
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(getContentPane(),
						"Une erreur s'est produite lors de la création des fichiers JSON", "Message d'erreur",
						JOptionPane.ERROR_MESSAGE);
			}
		};
	}
}
