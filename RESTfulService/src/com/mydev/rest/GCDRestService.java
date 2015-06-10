package com.mydev.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import com.mydev.myjpa.GCD;
import com.mydev.qualifier.Resource;


@Path("/services")
public class GCDRestService {
	
	@Inject
	private EntityManager entityManager;
	
	 @POST
	 @Path("/push")
	 @Produces("application/text")
	 public String push(@QueryParam("number1") int number1,@QueryParam("number2") int number2)  throws Exception
	 {
		 String destinationName = "jms/queue/GCDQueue";
		 Context ic = null;
		 ConnectionFactory  cf = null;
	     Connection connection =  null;
	     try{
	     GCD gcd=new GCD();
	     
		 gcd.setNumber1(number1);
		 gcd.setNumber2(number2);
		 
		 ArrayList<Integer> gcdList= new ArrayList<Integer>();
		 gcdList.add(gcd.getNumber1());
		 gcdList.add(gcd.getNumber2());
		 
		 entityManager=Resource.getEntityManager();
		 System.out.println(entityManager+"entityManager");
		 entityManager.getTransaction().begin();
		 entityManager.persist(gcd);
		 entityManager.flush();
		 entityManager.getTransaction().commit();
		 
		 ic = getInitialContext();
		 
         cf = (ConnectionFactory)ic.lookup("jms/RemoteConnectionFactory");
         Queue queue = (Queue)ic.lookup(destinationName);

         connection = cf.createConnection();
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer publisher = session.createProducer(queue);
         MessageConsumer subscriber = session.createConsumer(queue);

         connection.start();

         ObjectMessage  message = session.createObjectMessage();
         
         message.setObject((Serializable)gcdList);
         
         publisher.send(message);
        
         
         System.out.println("data"+gcd.getNumber1()+gcd.getNumber2());
         
         //message.reset();
         System.out.println("Message sent!");
		 
         ArrayList<Integer> listData = null;
         ObjectMessage objMessage = (ObjectMessage) subscriber.receive();
         listData = (ArrayList<Integer>) objMessage.getObject();
         System.out.println("listData: " + listData);
         
	     }
	     finally
	     {         
	            if(ic != null)
	            {
	                try
	                {
	                    ic.close();
	                    entityManager.close();
	                }
	                catch(Exception e)
	                {
	                    throw e;
	                }
	            }
	 
	            closeConnection(connection);
	      }
		 return "success";
	 }
	 
	 private void closeConnection(Connection con)
	    {      
	        try
	        {
	            if (con != null)
	            {
	                con.close();
	            }         
	        }
	        catch(Exception jmse)
	        {
	            System.out.println("Could not close connection " + con +" exception was " + jmse);
	            jmse.printStackTrace();
	        }
	    }
	 
	@GET 
	@Path("/get")
	@Produces("application/json")
	public List<Integer> list()
	{		
		List<Integer> lstInt;
		try{
		entityManager=Resource.getEntityManager();
		entityManager.getTransaction().begin();
		Query query= entityManager.createQuery("SELECT g.number1,g.number2 FROM GCD g");
		List<Object[]> lstGCD =query.getResultList();
		entityManager.getTransaction().commit();
		lstInt=new ArrayList<Integer>();
		for(Object[] obj:lstGCD){
			lstInt.add((Integer)obj[0]);
			lstInt.add((Integer)obj[1]);
		}
		System.out.println("lstInt"+lstInt);
		}
		finally{
			 if(entityManager != null)
	          {
				 entityManager.close();
	          }
		}
		return lstInt;
	}
	 
	public static Context getInitialContext( ) throws javax.naming.NamingException {
	 Properties p = new Properties( );
	 p.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
	 p.put(Context.INITIAL_CONTEXT_FACTORY,"org.jboss.naming.remote.client.InitialContextFactory");
	 p.put(Context.PROVIDER_URL, "remote://localhost:4447");
	 return new javax.naming.InitialContext(p);
	 } 
	 

}
