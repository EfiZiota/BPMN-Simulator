package simulation_gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.CardLayout;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JLayeredPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class ExampleFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JLayeredPane contentPane;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ExampleFrame frame = new ExampleFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	/**
	 * 
	 */
	public ExampleFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 590, 261);
		contentPane = new JLayeredPane();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		
		JButton next_button = new JButton("Επόμενο");
		next_button.setBounds(472, 190, 94, 23);
		next_button.setFont(new Font("Tahoma", Font.PLAIN, 11));
		next_button.setForeground(new Color(255, 255, 255));
		next_button.setBackground(new Color(0, 0, 0));
		next_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		contentPane.setLayout(null);
		contentPane.add(next_button);
		
		JButton cancel_button = new JButton("Άκυρο");
		cancel_button.setFont(new Font("Tahoma", Font.PLAIN, 11));
		cancel_button.setForeground(new Color(255, 255, 255));
		cancel_button.setBackground(new Color(0, 0, 0));
		cancel_button.setBounds(386, 190, 75, 23);
		contentPane.add(cancel_button);
		
		JLabel lblNewLabel = new JLabel("Προσθέστε ένα αρχείο .bpmn ενός μοντέλου που θέλετε να προσομοιώσετε.");
		lblNewLabel.setBounds(10, 70, 500, 35);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Προσθήκη Αρχείου");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.ITALIC, 14));
		lblNewLabel_1.setBounds(10, 39, 159, 28);
		contentPane.add(lblNewLabel_1);
		
		JButton AddFileButton = new JButton("+");
		
		AddFileButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				new ExcelOpenerGUI();
			}
		});
		AddFileButton.setForeground(new Color(255, 255, 255));
		AddFileButton.setBackground(new Color(0, 0, 0));
		AddFileButton.setBounds(140, 30, 45, 45);
		contentPane.add(AddFileButton);
		
		JLabel lblNewLabel_2 = new JLabel("Επιλέξτε το χρόνο που θα διαρκέσει η προσομοίωση.");
		lblNewLabel_2.setBounds(10, 128, 345, 23);
		contentPane.add(lblNewLabel_2);
		
		JComboBox comboBox = new JComboBox();
		comboBox.addItem("Ώρες");
		comboBox.addItem("Λεπτά");
		comboBox.addItem("Δευτερόλεπτα");
		comboBox.setToolTipText("");
		comboBox.setBounds(472, 128, 94, 22);
		contentPane.add(comboBox);
		
		textField = new JTextField();
		textField.setBounds(404, 129, 57, 20);
		contentPane.add(textField);
		textField.setColumns(10);
	}
}
