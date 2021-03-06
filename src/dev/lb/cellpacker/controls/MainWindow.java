package dev.lb.cellpacker.controls;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import dev.lb.cellpacker.Utils;
import dev.lb.cellpacker.structure.ResourceFile;
import dev.lb.cellpacker.structure.SearchableResourceViewManager;
import dev.lb.cellpacker.structure.view.ResourceView;
import dev.lb.cellpacker.structure.view.StaticResourceView;

public class MainWindow extends JFrame implements TreeSelectionListener, WindowListener, ComponentListener, DocumentListener{
	private static final long serialVersionUID = 3681709759315746587L;
	
	private JSplitPane split;
	
	private JTree tree;
	private SearchableResourceViewManager view;
	private JMenu resourceMenu;
	private JTextField searchField;
	private boolean fixCDB;
	
	public MainWindow(){
		super("Cellpacker Editor 2.1");
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		fixCDB = true;
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new StaticResourceView("No Files", "No files loaded", new byte[0]));
		tree =  new JTree(root);
		tree.setSelectionPath(new TreePath(root));
		tree.addTreeSelectionListener(this);
		this.setPreferredSize(new Dimension(800, 600));

		JPanel searchCon = new JPanel();
		searchCon.setLayout(new BoxLayout(searchCon, BoxLayout.X_AXIS));
		searchCon.setBorder(new EmptyBorder(3, 5, 5, 2));
		JLabel fixedDesc = new JLabel("Search: ");
		searchField = new JTextField();
		searchField.getDocument().addDocumentListener(this);
		fixedDesc.setMaximumSize(fixedDesc.getPreferredSize());
		searchCon.add(fixedDesc);
		searchCon.add(searchField);
		
		JPanel rightCon = new JPanel(new BorderLayout());
		rightCon.add(new JScrollPane(tree), BorderLayout.CENTER);
		rightCon.add(searchCon, BorderLayout.SOUTH);
		split.setRightComponent(rightCon);
		
		
		JMenuItem reuseable;
		JMenuBar menu = new JMenuBar();
		JMenu file = new JMenu("File");
		//OPEN
		reuseable = new JMenuItem("Open");
		reuseable.addActionListener((e) -> {
			if(view != null){
				if(JOptionPane.showConfirmDialog(this, "A resource file is already opened. Unsaved changes will be lost. Do you want to continue?", "Open Resource",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION){
					return;
				}
			}
			JFileChooser jfc =  new JFileChooser(".");
			jfc.setFileFilter(new FileNameExtensionFilter("Dead Cells Resource File", "*.pak", "pak", ".pak"));
			int result = jfc.showOpenDialog(this);
			if(result == JFileChooser.APPROVE_OPTION && jfc.getSelectedFile() != null){
				setResourceFileOnStart(ResourceFile.fromFile(jfc.getSelectedFile()));
			}
		});
		reuseable.setToolTipText("Open a Dead Cells resource file to view its contents (usually called res.pak)");
		file.add(reuseable);
		//SAVE
		reuseable = new JMenuItem("Save");
		reuseable.setToolTipText("Save the currently opened resource file with all modifications. (To use in-game, simply replace the original res.pak with this one)");
		reuseable.addActionListener((e) -> {
			if(view == null){
				JOptionPane.showMessageDialog(this, "No resource is opened", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			JFileChooser jfc = new JFileChooser();
			jfc.setFileFilter(new FileNameExtensionFilter("Dead Cells Resource File", "*.pak", "pak", ".pak"));
			if(jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION && jfc.getSelectedFile() != null){
				view.buildFileWithChanges().writeToFile(jfc.getSelectedFile());
			}
		});
		file.add(reuseable);
		file.addSeparator();
		//EXPORTALL
		reuseable = new JMenuItem("Export all");
		reuseable.setToolTipText("Export all resources to a folder. (and create a header template file for future imports)");
		reuseable.addActionListener((e) -> {
			if(view == null){
				JOptionPane.showMessageDialog(this, "No resource is opened", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			JFileChooser jfc = new JFileChooser();
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if(jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION && jfc.getSelectedFile() != null){
				view.buildFileWithChanges().writeAllResources(jfc.getSelectedFile());
			}
		});
		file.add(reuseable);
		file.addSeparator();
		JCheckBoxMenuItem reuseable2 = new JCheckBoxMenuItem("Create fixed data.cdb", true);
		reuseable2.setToolTipText("The version of data.cdb included in res.pak can not be directly read by CastleDB. This will create a fixed version called data.exported.cdb");
		reuseable2.addChangeListener((e) ->{
			fixCDB = reuseable2.isSelected();
		});
		file.add(reuseable2);
		file.addSeparator();
		//CLOSE
		reuseable = new JMenuItem("Close");
		reuseable.setToolTipText("Closes the currently opened resource file");
		reuseable.addActionListener((e) -> {
			if(view == null){
				JOptionPane.showMessageDialog(this, "No resource is opened", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if(JOptionPane.showConfirmDialog(this, "<html>Unsaved changes will be lost.<br>Are you sure you want to close the file?", "Close file", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
				view = null;
				DefaultMutableTreeNode root2 = new DefaultMutableTreeNode(new StaticResourceView("No Files", "No files loaded", new byte[0]));
				((DefaultTreeModel) tree.getModel()).setRoot(root2);
				tree.setSelectionPath(new TreePath(root));
			}
		});
		file.add(reuseable);
		//QUIT
		reuseable = new JMenuItem("Quit");
		reuseable.setToolTipText("Quit the program. Unsaved changes will be lost.");
		reuseable.addActionListener((e) -> {
			if(JOptionPane.showConfirmDialog(this, "<html>Unsaved changes will be lost.<br>Are you sure you want to quit?", "Quit program", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
				this.dispose();
			}
		});
		file.add(reuseable);
		file.setBorder(new EmptyBorder(0, 5, 0, 5));
		menu.add(file);
		
		JMenu edit = new JMenu("Edit");
		resourceMenu = new JMenu("Resource Options");
		resourceMenu.add("<No options available>");
		edit.add(resourceMenu);
		edit.addSeparator();
		//RESTORE
		reuseable = new JMenuItem("Restore all resources");
		reuseable.setToolTipText("Restore all resources to their original state");
		reuseable.addActionListener((e) -> {
			if(view == null){
				JOptionPane.showMessageDialog(this, "No resource is opened", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if(JOptionPane.showConfirmDialog(this, "<html>Unsaved changes will be lost.<br>Are you sure you want to restore all resources?", "Restore resources", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
				//view.getViewsUnsorted().forEach((v) -> v.restoreAllResources(this));
			}
		});
		edit.add(reuseable);
		//RELOAD
		reuseable = new JMenuItem("Reload current resource");
		reuseable.setToolTipText("Reloads this component and the display elements.");
		reuseable.addActionListener((e) -> {
			if(view == null){
				JOptionPane.showMessageDialog(this, "No resource is opened", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			ResourceView rv = view.getResourceView(tree.getSelectionPath());
			if(rv == null){
				JOptionPane.showMessageDialog(this, "No resource is selected", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			rv.focusLost();
			rv.forceInit();
		});
		edit.add(reuseable);
		//SEARCH
		reuseable = new JMenuItem("Seach for resource");
		reuseable.setToolTipText("Search for a resource view");
		reuseable.addActionListener((e) -> {
			String term = JOptionPane.showInputDialog(this, "Search:", "Search for resource view", JOptionPane.QUESTION_MESSAGE);
			searchField.setText(term);
		});
		edit.add(reuseable);
		edit.setBorder(new EmptyBorder(0, 5, 0, 5));
		menu.add(edit);
		JMenu help = new JMenu("Help");
		reuseable = new JMenuItem("Show tutorial");
		reuseable.addActionListener((e) -> {
			Utils.showAboutDialog("<html>For more information read the Readme on the github page:<br>https://github.com/ReBuilders101/CellPacker/blob/master/README.md", "About / Help", "https://github.com/ReBuilders101/CellPacker/blob/master/README.md");
		});
		help.add(reuseable);
		help.addSeparator();
		reuseable = new JMenuItem("Show source code");
		reuseable.setToolTipText("The source code of this project is available on GitHub");
		reuseable.addActionListener((e) -> {
			Utils.showAboutDialog("<html>The source code of this project is available on GitHub:<br>https://github.com/ReBuilders101/CellPacker", "Source code", "https://github.com/ReBuilders101/CellPacker");
		});
		help.add(reuseable);
		help.addSeparator();
		reuseable = new JMenuItem("About");
		reuseable.setToolTipText("Show the projects readme-file");
		reuseable.addActionListener((e) -> {
			Utils.showAboutDialog("<html>Open the projects readme-file:<br>https://github.com/ReBuilders101/CellPacker/blob/master/README.md", "Source code", "https://github.com/ReBuilders101/CellPacker/blob/master/README.md");
		});
		help.add(reuseable);
		reuseable = new JMenuItem("About CastleDB");
		reuseable.setToolTipText("Information about CastleDB, the database used by Dead Cells");
		reuseable.addActionListener((e) -> {
			Utils.showAboutDialog("<html>CastleDB is used by Dead Cells to store information about levels, monsters, items and more.<br>CastleDB can be used to open and edit the file data.exported.cdb more easily:<br>http://castledb.org/", "Source code", "http://castledb.org/");
		});
		help.add(reuseable);
		help.setBorder(new EmptyBorder(0, 5, 0, 5));
		menu.add(help);
		
		split.setLeftComponent(((ResourceView) root.getUserObject()).getDisplay());
		
		split.setDividerLocation(0.8);
		
		this.setJMenuBar(menu);
		this.add(split);
		this.addWindowListener(this);
		this.addComponentListener(this);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		this.pack();
	}

	public void setResourceFileOnStart(ResourceFile file){
		view = null;
		view = new SearchableResourceViewManager(file);
		view.setTree(tree);
		view.setSearchString("", tree);
		tree.setSelectionPath(new TreePath(tree.getModel().getRoot()));
	}
	
	public boolean fixCDB(){
		return fixCDB;
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		if(e.getOldLeadSelectionPath() != null){
			ResourceView ro = (ResourceView) ((DefaultMutableTreeNode) e.getOldLeadSelectionPath().getLastPathComponent()).getUserObject();
			ro.focusLost();
		}
		
		ResourceView rv = (ResourceView) (
				((DefaultMutableTreeNode) e.getPath().getLastPathComponent()).getUserObject());
		split.setLeftComponent(rv.getDisplay());
		split.setDividerLocation(0.8);

		resourceMenu.removeAll();
		for(JMenuItem menu : rv.getContextMenu()){
			if(menu.getText().equals("$Sep$")){
				resourceMenu.addSeparator();
			}else{
				resourceMenu.add(menu);
			}
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if(JOptionPane.showConfirmDialog(this, "<html>Unsaved changes will be lost.<br>Are you sure you want to quit?", "Quit program", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
			if(tree.getSelectionPath() != null)
				((ResourceView )((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent()).getUserObject()).focusLost();
			this.dispose();
		}
	}

	@Override
	public void componentResized(ComponentEvent e) {
		split.setDividerLocation(0.8);
	}
	
	@Override
	public void changedUpdate(DocumentEvent e) {
		if(tree.getSelectionPath() != null){
			ResourceView ro = (ResourceView) ((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent()).getUserObject();
			ro.focusLost();
		}

		tree.setSelectionPath(new TreePath(tree.getModel().getRoot()));
		if(view != null) view.setSearchString(searchField.getText(), tree);
	}
	
	@Override 
	public void insertUpdate(DocumentEvent e) {
		changedUpdate(e);
	}
	
	@Override
	public void removeUpdate(DocumentEvent e) {
		changedUpdate(e);
	}
	
	@Override public void windowActivated(WindowEvent e) {}
	@Override public void windowDeactivated(WindowEvent e) {}
	@Override public void windowDeiconified(WindowEvent e) {}
	@Override public void windowIconified(WindowEvent e) {}
	@Override public void windowOpened(WindowEvent e) {}
	@Override public void windowClosed(WindowEvent e) {}
	@Override public void componentHidden(ComponentEvent e) {}
	@Override public void componentMoved(ComponentEvent e) {}
	@Override public void componentShown(ComponentEvent e) {}
}
