package gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import dao.DBUtil;
import util.FileSaver;

public class MainWindow extends JFrame {
	private static final long serialVersionUID = -2278436951424873713L;
	private static final Font FONT = new Font("Calibri Light", Font.PLAIN, 15);
	private static String dbName;
	private JComboBox<String> databasesComboBox;
	private JList<String> listRelationships;

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
		btnSubmit.setBounds(131, 338, 196, 40);
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
	}

	private void populateDatabasesComboBox() {
		DBUtil.getDatabasesNames().forEach(name -> databasesComboBox.addItem(name));
	}

	private ActionListener databasesComboBoxActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dbName = (String) databasesComboBox.getSelectedItem();
				List<String> list = DBUtil.getRelationshipsBetweenTables(dbName);
				listRelationships.setListData(list.toArray(new String[list.size()]));
			}
		};
	}

	private ActionListener btnSubmitActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (DBUtil.getRelatedTablesData(dbName).isEmpty()) {
					JOptionPane.showMessageDialog(getContentPane(),
							"Elle n'existe aucune relation entre les tables de la base de donneés sélectionée", "Message",
							JOptionPane.WARNING_MESSAGE);
					return;
				}

				try {
					String location = FileSaver.saveDataAsJSON(dbName, DBUtil.getRelatedTablesData(dbName));
					JOptionPane.showMessageDialog(getContentPane(),
							"Les fichiers json sont creer avec succès dans le dossier :\n" + location);
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(getContentPane(),
							"Une erreur s'est produite lors de la création des fichiers JSON", "Message d'erreur",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};
	}
}
