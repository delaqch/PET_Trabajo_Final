import java.io.*;  
import java.net.*;
import java.util.ArrayList;  
public class Conex {  
	//Cliente
	public static void main(String[] args){
		int proccessCounter = 0;
		//Procesos que se ejecutaran en paralelo
		ArrayList<Process> process = new ArrayList<Process>();
		while(true)
		{	try{  
			ServerSocket ss=new ServerSocket(6666);  
			Socket s=ss.accept();//establishes connection   
			DataInputStream dis=new DataInputStream(s.getInputStream());
			DataOutputStream dout=new DataOutputStream(s.getOutputStream());
			String  str=(String)dis.readUTF();  
			System.out.println("message= "+str);  
			ss.close();
			
			String[] c = str.split("\\s");
			if(c[0].equals("start")){
				try {
					process.add(Runtime.getRuntime().exec(new String[]{"bash","-c",c[1]}));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					System.out.println("Error");
					e1.printStackTrace();
				}
				dout.writeInt(proccessCounter);
				dout.flush();
				proccessCounter ++ ;
			}else{
				int pId = Integer.parseInt(c[1]);
				System.out.println("kill= "+pId);
				process.get(pId).destroy();
				dout.close();  
				
			}
			  
			}catch(Exception e){
				System.out.println("Erro2");
				System.out.println(e);
			}
		}
	}  
} 