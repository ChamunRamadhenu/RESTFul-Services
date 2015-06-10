package com.mydev.qualifier;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.EntityManagerFactory;

public class Resource {
	private static EntityManagerFactory factory;
	
	@Produces
	public static EntityManager getEntityManager()
	{
		factory=Persistence.createEntityManagerFactory("GCDPersistence");
		EntityManager em=factory.createEntityManager();
		return em;
	}
	

}
