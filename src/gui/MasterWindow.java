package gui;

import javax.swing.JButton;
import javax.swing.JFrame;

public class MasterWindow extends JFrame{
    private static final long serialVersionUID = 1L;
    public static void main(String[] args){
        new MasterWindow().setVisible(true);
    }

    private MasterWindow(){
        super("Seach Engine");
        setSize(600,600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(3);

        JFrame mainFrame = new JFrame("Search Frame");
        JButton searchButton = new JButton("Search");

        mainFrame.getContentPane().add(searchButton);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

}

