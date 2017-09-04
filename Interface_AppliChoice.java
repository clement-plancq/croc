import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;


public class Interface_AppliChoice{
	
	public Interface_AppliChoice(){
		final JFrame window = new JFrame();
	    window.setTitle("CROC - Coreference Resolution for Oral Corpora");
	    window.setSize(600, 200);
	    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    window.setLocationRelativeTo(null);
	    JPanel top = new JPanel();
	    top.setBorder(new LineBorder(Color.BLACK));
	    JLabel jlabel = new JLabel("Sélectionner une application");
	    jlabel.setFont(new Font("Dialog", 1, 15));
	    top.setSize(50,50);
	    top.add(jlabel);
	    window.add(top,BorderLayout.NORTH);
	    window.setLayout(new GridLayout(2, 1));
	    JPanel test = new JPanel();
	    test.setLayout(new GridLayout(1, 2));
	    JButton choice1 = new JButton("Créer un modèle de classification");
	    JButton choice2 = new JButton("Détecter des chaînes de coréférence");
	    test.add(choice1);
	    test.add(choice2);
	    window.add(test);
	    window.setVisible(true);
	    
	    choice1.addActionListener(new ActionListener(){
	        public void actionPerformed(ActionEvent event){
	          try {
	        	  window.dispose();
	        	  new Interface_newModel();
	          } 
	          catch (Exception e) {e.printStackTrace();}
	        }
	      });

	    choice2.addActionListener(new ActionListener(){
	        public void actionPerformed(ActionEvent event){
	          try {
	        	window.dispose();
	        	new Interface_CoreferenceResolver();
	          }
	          catch (Exception e) {e.printStackTrace();}
	        }
	      });
	}
}

