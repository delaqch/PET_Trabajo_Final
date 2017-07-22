import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.event.ActionEvent;
import javax.swing.JTable;

public class Servidor {

	private JFrame frame;
	private JTextField ipEsclavo;
	private JTextField aliasEsclavo;
	private JTable hostsTable;
	Esclavos e = new Esclavos();
	Tareas t = new Tareas();
	private JTable hostTareas;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Servidor window = new Servidor();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private static class JTableButtonRenderer implements TableCellRenderer {        
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JButton button = (JButton)value;
            return button;  
        }
    }
	
	private static class JTableButtonMouseListener extends MouseAdapter {
        private final JTable table;

        public JTableButtonMouseListener(JTable table) {
            this.table = table;
        }

        public void mouseClicked(MouseEvent e) {
            int column = table.getColumnModel().getColumnIndexAtX(e.getX()); // get the coloum of the button
            int row    = e.getY()/table.getRowHeight(); //get the row of the button

                    /*Checking the row or column is valid or not*/
            if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
                Object value = table.getValueAt(row, column);
                if (value instanceof JButton) {
                    /*perform a click event*/
                    ((JButton)value).doClick();
                }
            }
        }
    }
		
	class EjecutadorTerminador extends Thread {
		public void run() {
			while(true) {
			for (Proceso p : t.procesos) {
				if (p.horaInicio.before(new Date()) && p.estado.equals("Pendiente")) {
					int e = StartProcess(p.host, p.proceso);
					if (e != -1 ) {
                    	p.estado = "Iniciado";
                    	p.id = e;
                        t.fireTableDataChanged();
                    }
				}
			}
			}
				
				
		}
	}
	
	
	class Proceso {
		int id;
		String host;
		String alias;
		String proceso;
		String estado;
		Date horaInicio;
		Date horaFin;
		Proceso(int i, String h, String a, String p, String s, Date d){
			id = i;
			host = h;
			alias = a;
			proceso = p;
			estado = s;
			horaInicio = d;
			horaFin = new Date();
		}
	}
	
	class Tareas extends AbstractTableModel{
		String[] COLUMN_NAMES = new String[] {"Id", "Host" , "Alias" , "Proceso", "Estado" , "Inicio", "Fin" , "Button1"};
		Class<?>[] COLUMN_TYPES = new Class<?>[] {int.class, String.class, String.class,String.class,String.class,String.class,String.class, JButton.class};
		int columnas = 8;
		int filas = 0;
		List<Proceso> procesos = new ArrayList<>();
		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return columnas;
		}

		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return filas;
		}
        @Override public String getColumnName(int columnIndex) {
            return COLUMN_NAMES[columnIndex];
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return COLUMN_TYPES[columnIndex];
        }

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return procesos.get(rowIndex).id;
			case 1:
				return procesos.get(rowIndex).host;
			case 2:
				return procesos.get(rowIndex).alias;
			case 3:
				return procesos.get(rowIndex).proceso;
			case 4:
				return procesos.get(rowIndex).estado;
			case 5:
				return procesos.get(rowIndex).horaInicio.toString();
			case 6:
				return procesos.get(rowIndex).horaFin.toString();
			case 7:
				final JButton button = new JButton("Detener");
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        int e = StopProcess(procesos.get(rowIndex).host, procesos.get(rowIndex).id);
                        if (e != -1 ) {
                        	procesos.get(rowIndex).id = e;
                        	procesos.get(rowIndex).estado = "Terminado";
                            fireTableDataChanged();
                        }else {
                        	JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(button), "Error al terminar la tarea");
                        }
                        
                    	
                    }
                });
                return button;
			}
			return null;
		}
		
		public void AgregarProceso(int i ,   String host, String alias, String t, String state, Date d) {
			procesos.add(new Proceso(i, host, alias, t, state, d));
			filas++;
		}
	}
	
	class Esclavos extends AbstractTableModel{
		String[] COLUMN_NAMES = new String[] {"Ip", "Alias", "Button1"};
		Class<?>[] COLUMN_TYPES = new Class<?>[] {String.class, String.class, JButton.class};
		int columnas = 3;
		int filas = 0;
		private Map<String, String> hosts;
		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return columnas;
		}

		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return filas;
		}
        @Override public String getColumnName(int columnIndex) {
            return COLUMN_NAMES[columnIndex];
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return COLUMN_TYPES[columnIndex];
        }

		@Override
		public Object getValueAt(int arg0, int arg1) {
			// TODO Auto-generated method stub
			int c = 0;
			for(String key: hosts.keySet()) {
				if(c == arg0) {
					if(arg1 == 0) {
						return hosts.get(key);
					}else if (arg1 == 1) {
						return key;
					}else if (arg1 == 2) {
						final JButton button = new JButton("Iniciar");
                        button.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent arg0) {
                                System.out.println(key);
                                JFrame frame = new JFrame("Ingrese comando a ejecutar");
                                String name = JOptionPane.showInputDialog(frame, "Comando");
                                String date = JOptionPane.showInputDialog(frame, "Hora de Ejecucion");
                                DateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                Date d = null;
                                if(date.equals("ahora")) {
                                	d = new Date();
                                	int e = StartProcess(hosts.get(key), name);
                                    if (e != -1 ) {
                                    	t.AgregarProceso(e,hosts.get(key), key, name, "Iniciado", d);
                                        t.fireTableDataChanged();
                                    }else {
                                    	JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(button), "Error al iniciar la tarea");
                                    }
                                }else {
                                	try {
										d = formato.parse(date);
									} catch (ParseException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
                                		t.AgregarProceso(-1,hosts.get(key), key, name, "Pendiente", d);
                                        t.fireTableDataChanged();
                                    
                                }
                                System.out.println(name);
                            }
                        });
                        return button;
					}
				}
				c++;
			}
			return "objeto de la tabla";
		}
		
		public void Agregar(String ip, String alias) {
			hosts.put(alias, ip);
			filas ++;
		}
		
		
		Esclavos (){
			hosts = new HashMap<String, String>();
		}
	}

	/**
	 * Create the application.
	 */
	public Servidor() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 824, 503);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JButton btnAgregarEsclavo = new JButton("Agregar Esclavo");
		btnAgregarEsclavo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				e.Agregar(ipEsclavo.getText(), aliasEsclavo.getText());
				e.fireTableDataChanged();
			}
		});
		btnAgregarEsclavo.setBounds(281, 12, 139, 27);
		frame.getContentPane().add(btnAgregarEsclavo);
		
		ipEsclavo = new JTextField();
		ipEsclavo.setBounds(12, 15, 114, 21);
		frame.getContentPane().add(ipEsclavo);
		ipEsclavo.setColumns(10);
		
		aliasEsclavo = new JTextField();
		aliasEsclavo.setBounds(155, 15, 114, 21);
		frame.getContentPane().add(aliasEsclavo);
		aliasEsclavo.setColumns(10);
		
		hostsTable = new JTable();
		hostsTable.setBounds(22, 51, 772, 87);
		frame.getContentPane().add(hostsTable);
		
		hostsTable.setModel(e);
		TableCellRenderer buttonRenderer = new JTableButtonRenderer();
        hostsTable.getColumn("Button1").setCellRenderer(buttonRenderer);
        hostsTable.addMouseListener(new JTableButtonMouseListener(hostsTable)); 
        
        hostTareas = new JTable();
        hostTareas.setBounds(22, 165, 772, 285);
        frame.getContentPane().add(hostTareas);
        
        hostTareas.setModel(t);
        hostTareas.getColumn("Button1").setCellRenderer(buttonRenderer);
        hostTareas.addMouseListener(new JTableButtonMouseListener(hostTareas));
        //Iniciar hilo 
        EjecutadorTerminador t = new EjecutadorTerminador();
        t.start();
	}
	
	int StartProcess(String host, String process ) {
		try {
		Socket s=new Socket(host,6666);  
		DataOutputStream dout=new DataOutputStream(s.getOutputStream());
		DataInputStream dis=new DataInputStream(s.getInputStream());
		dout.writeUTF("start " + process);
		dout.flush();  
		int code=(int)dis.readInt();
		System.out.println("id= "+ code);
		dout.close();  
		dis.close();
		s.close();
		return code;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
		
	int StopProcess(String host, int process ) {
		try {
		Socket s=new Socket(host,6666);  
		DataOutputStream dout=new DataOutputStream(s.getOutputStream());
		DataInputStream dis=new DataInputStream(s.getInputStream());
		dout.writeUTF("stop " + process);
		dout.flush();  
		dout.close();  
		dis.close();
		s.close();
		return 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

}
