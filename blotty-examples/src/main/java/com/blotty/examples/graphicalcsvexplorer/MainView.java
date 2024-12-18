package com.blotty.examples.graphicalcsvexplorer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.models.AbstractDataModel;
import com.blotty.core.models.Column;
import com.blotty.core.models.ColumnsModel;
import com.blotty.core.models.Row;

public class MainView extends JFrame {

	private MainViewController controller;
	private JTable mainTable;
	private JComboBox<String> columnFilter;
	
	public MainView() {
		setTitle("CSV Explorer");
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		createTable();
		createMenu();
		createContent();
	}

	private void createMenu() {
		JMenuBar mb =new JMenuBar();  
		JMenu menu=new JMenu("Files");  
		JMenuItem openCsv=new JMenuItem("Load CSV");  

		openCsv.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select a .CSV File");
					FileNameExtensionFilter filter = new FileNameExtensionFilter("csv", "CSV");
					fileChooser.addChoosableFileFilter(filter);
					int option = fileChooser.showOpenDialog( MainView.this );
					if (option == JFileChooser.APPROVE_OPTION) {
						controller.loadCsv( fileChooser.getSelectedFile() );
					}
				} catch (Exception ex ) {
					ex.printStackTrace();
				}
			}
		});

		menu.add(openCsv);
		mb.add(menu);
		this.setJMenuBar(mb);  
	}

	private void createContent() {
		getContentPane().setLayout( new BorderLayout() );	
		getContentPane().add( new JScrollPane(createFilteringPanel()), BorderLayout.NORTH );
		
		JPanel tablePanel = new JPanel( new BorderLayout() );	
		tablePanel.add( mainTable.getTableHeader(), BorderLayout.NORTH );
		tablePanel.add( new JScrollPane(mainTable), BorderLayout.CENTER );
		
		getContentPane().add( tablePanel, BorderLayout.CENTER );
	}

	private JComponent  createFilteringPanel() {
		columnFilter = new JComboBox<String>();
		columnFilter.setEnabled(true);
		columnFilter.setEditable(false);
		
		JComboBox<String> operators = new JComboBox<>(new String[] {"=", "!=", ">", ">=", "<", "<=", "LIKE", "NOT LIKE"});
		operators.setEnabled(true);
		operators.setEditable(false);
		
		JTextField filterValue = new JTextField(20);
		filterValue.setEditable(true);
		
		JButton applyFilter = new JButton("Apply Filter");
		
		JPanel filteringPanel = new JPanel();
		filteringPanel.setBorder( new TitledBorder("Filters") );
		filteringPanel.setLayout( new FlowLayout(FlowLayout.LEFT));
		filteringPanel.add(columnFilter);
		filteringPanel.add(operators);
		filteringPanel.add(filterValue);
		filteringPanel.add(applyFilter);
		
		applyFilter.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					controller.applyFilter(
						(String)columnFilter.getSelectedItem(),
						(String)operators.getSelectedItem(),
						filterValue.getText()
					);
				} catch (Exception ex ) {
					ex.printStackTrace();
				}
			}
		});
		return filteringPanel;
	}
	
	private JComponent  createTable() {
		mainTable = new JTable();	
		mainTable.getTableHeader().setDefaultRenderer(new CustomTableHeaderRenderer(mainTable.getTableHeader().getDefaultRenderer()));
		return mainTable;
	}

	public void setController(MainViewController ctr) {
		this.controller = ctr;
	}

	public static class CustomTableHeaderRenderer implements TableCellRenderer {
		private static final Font labelFont = new Font("Arial", Font.BOLD, 11);

		private TableCellRenderer delegate;

		public CustomTableHeaderRenderer(TableCellRenderer delegate) {
			this.delegate = delegate;
		} 
		
		private Icon sortingIcon = null;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component c = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if(c instanceof JLabel) {
				JLabel label = (JLabel) c;
				label.setFont(labelFont);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				label.setBorder(BorderFactory.createEtchedBorder());
				
				if ( sortingIcon!=null ) {
					label.setIcon(sortingIcon);
				}
			}
			return c;
		}

		public Icon getSortingIcon() {
			return sortingIcon;
		}

		public void setSortingIcon(Icon sortingIcon) {
			this.sortingIcon = sortingIcon;
		}
	}

	public void setTableColumns( final ColumnsModel colModel) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					DefaultTableModel tableModel = new DefaultTableModel();
					tableModel.addColumn("PK");

					columnFilter.removeAllItems();
					
					for(Column col : colModel.getColumns()){
						tableModel.addColumn(col.getName());
						columnFilter.addItem(col.getName());
					}
					
					mainTable.setModel(tableModel);  
					mainTable.getTableHeader().addMouseListener(new MouseAdapter() {

						int[] clicksCount = new int[colModel.getColumnsCount()+1];

						Icon ascSortIcon = UIManager.getIcon("Table.ascendingSortIcon");
						Icon descSortIcon = UIManager.getIcon("Table.descendingSortIcon");

						@Override
						public void mouseClicked(MouseEvent event) {
							try {
								// Apply sorting
								Point point = event.getPoint();
								int colId = mainTable.columnAtPoint(point);
								boolean asc = (++clicksCount[colId])%2==0;
								controller.applySorting( colId, asc);
								
								((CustomTableHeaderRenderer)mainTable.getTableHeader().getDefaultRenderer()).setSortingIcon(
									(asc)?ascSortIcon:descSortIcon
								);
					
							} catch (Exception ex ) {
								ex.printStackTrace();
							}
						}
					});
				} catch (Exception ex ) {
					ex.printStackTrace();
				}
			}
		});
	}

	public void setTableData( final AbstractDataModel dataModel) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					DefaultTableModel model = (DefaultTableModel)mainTable.getModel();
					model.setRowCount(0);

					dataModel.stream( new IConsumer<Row>() {

						private Object[] rowWithKey( Row row ) {
							Object[] ret = new Object[row.getFields().length+1];
							ret[0] = row.getKey();
							System.arraycopy(row.getFields(), 0, ret, 1, row.getFields().length);
							return ret;
						}

						@Override
						public boolean stream(Row row) throws RowsModelException {
							model.addRow( rowWithKey(row));
							return true;
						}            			
					});

				} catch (Exception ex ) {
					ex.printStackTrace();
				}
			}
		});		
	}
}
