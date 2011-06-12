/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.thrift.TException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

import java.util.*;

public class JavaClient {
  static boolean perfectPlay = true;

  public static void main(String [] args) {

    if (args.length != 1) {
      System.out.println("Please enter 'simple' or 'secure'");
      System.exit(0);
    }

    try {
      TTransport transport;
      if (args[0].contains("simple")) {
        transport = new TSocket("localhost", 9090);
        transport.open();
      }
      else {
        /*
         * Similar to the server, you can use the parametrs to setup client parameters or
         * use the default settings. On the client side, you will need a TrustStore which
         * contains the trusted certificate along with the public key. 
         * For this example it's a self-signed cert. 
         */
        TSSLTransportParameters params = new TSSLTransportParameters();
        params.setTrustStore("../../lib/java/test/.truststore", "thrift", "SunX509", "JKS");
        /*
         * Get a client transport instead of a server transport. The connection is opened on
         * invocation of the factory method, no need to specifically call open()
         */
        transport = TSSLTransportFactory.getClientSocket("localhost", 9091, 0, params);
      }

      TProtocol protocol = new  TBinaryProtocol(transport);
      SimonSays.Client client = new SimonSays.Client(protocol);

      perform(client);

      transport.close();
    } catch (TException x) {
      x.printStackTrace();
    } 
  }

  private static void perform(SimonSays.Client client) throws TException
  {
	  if(client.registerClient("thriftwork@telegraphics.com.au")){
		  System.out.println("starting the game!");
		  do{
			  System.out.print("new turn... received <");
			  List<Color> colours = client.startTurn();
			  for(Color c : colours)
				  System.out.print(" "+c);
			  System.out.println(" >");

			  if(perfectPlay){
				  for(Color c : colours){
					  System.out.println("  echoing colour "+c);
					  client.chooseColor(c);
				  }
			  }else{
				  double p = 0.99;
				  for(Color c : colours){
					  if(Math.random() > p)
						  c = Color.findByValue((int)Math.ceil(Color.values().length*Math.random()));
					  System.out.print("  trying colour "+c);
					  if(client.chooseColor(c)){
						  System.out.println(" ...ok");
					  }else{
						  System.out.println(" ...oops");
						  break;
					  }
					  p *= 0.95; // chance of a mistake increases after each choice
				  }
			  }
		  }while(!client.endTurn());
		  System.out.println("finished the game!");
		  System.out.println("winGame key: " + client.winGame());
	  }else
		  System.err.println("failed to register");
  }
}
