package org.safs.install;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConsumOutStreamProcess {
	public static final int PROCESS_NORMAL_END 				= 0;
	public static final int PROCESS_COMMAND_NOT_INITIAL 	= -1;
	public static final int PROCESS_COMMAND_EXECUTE_ERR	 	= -2;
	
	public static final int WAIT_FOREVER				 	= 0;
	
	private String command;
	//If printOutput is true, the process's stdout and stderr will be printed.
	private boolean printOutput;
	private int timeout=WAIT_FOREVER;
	private boolean useTimeout;
	
	/**
	 * @param command		The shell command to be executed.
	 * @param printOutput	True, the process's stdout and stderr will be printed.
	 * @param useTimeout	Ture, wait end of process for timeout seconds.
	 * 						Call setTimeout(timeout) to set the timeout.
	 */
	public ConsumOutStreamProcess(String command,boolean printOutput,boolean useTimeout){
		this.command = command;
		this.printOutput = printOutput;
		this.useTimeout = useTimeout;
	}
	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}
	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}
	/**
	 * timeout should be set to a NON-negative int value or 0
	 * If timeout is 0, will wait forever until the process end.
	 * Else, will wait timeout seconds
	 * @param timeout
	 */
	public void setTimeout(int timeout){
		this.timeout = timeout;
	}
	
	public int start(){
		int executStatus = PROCESS_NORMAL_END;
		
		if(command==null || command.equals("")){
			System.out.println("You should initial the command with constructor ProcessWithOutStreamConsum(command).");
			return PROCESS_COMMAND_NOT_INITIAL;
		}
		Runtime runtime = Runtime.getRuntime();
		try {
			Process process = runtime.exec(command);
			//In some OS, the stdout and stderr buffer is small, we should read it out.
			//If the buffer is full, the process will never end, that is to say the method
			//process.waitFor() will hang for ever until the buffer is clean (so that process
			//can continue to write to it.)
			OutputBufferConsumer stdoutConsumer = new OutputBufferConsumer(process.getInputStream(),OutputBufferConsumer.FROM_STDOUT);
			OutputBufferConsumer stderrConsumer = new OutputBufferConsumer(process.getErrorStream(),OutputBufferConsumer.FROM_STDERR);
			stdoutConsumer.start();
			stderrConsumer.start();

			if(useTimeout){
				int i=0;
				while ( timeout==WAIT_FOREVER || i<timeout){
					try{
						//If the process has not terminated, calling method exitValue() will cause
						//an IllegalThreadStateException
						executStatus = process.exitValue();
						break;
					}catch(IllegalThreadStateException e){
						System.out.println("IllegalThreadStateException");
					}
					i++;
				    Thread.sleep(1000);
				}
			}else{
				//The method waitFor() will block the current thread until the process terminate.
				//If the process terminate normally, it will return 0.
				executStatus = process.waitFor();
			}
			
			if(stderrConsumer.isAlive())
				stderrConsumer.interrupt();
			if(stdoutConsumer.isAlive())
				stdoutConsumer.interrupt();
		} catch (IOException e) {
			System.out.println("IOException occur: "+e.getMessage());
			executStatus = PROCESS_COMMAND_EXECUTE_ERR;
		} catch (InterruptedException e) {
			System.out.println("InterruptedException occur: "+e.getMessage());
			executStatus = PROCESS_COMMAND_EXECUTE_ERR;
		}
		
		return executStatus;
	}
	
	class OutputBufferConsumer extends Thread{
		public static final String FROM_STDOUT = "FROM_STDOUT";
		public static final String FROM_STDERR = "FROM_STDERR";
		
		private InputStream in;
		private String streamFromWhere;

		public OutputBufferConsumer(InputStream in,String streamFromWhere){
			this.in = in;
			this.streamFromWhere = streamFromWhere;
		}
		
		public void run(){
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);
			String aLine = "";
			try {
				while((aLine=br.readLine())!=null){
					if(printOutput){
						if(streamFromWhere.equals(FROM_STDERR)){
							System.err.println(aLine);
						}else if(streamFromWhere.equals(FROM_STDOUT)){
							System.out.println(aLine);
						}
					}
				}
			} catch (IOException e) {
				//Just ignore
			}
		}
	}
}
