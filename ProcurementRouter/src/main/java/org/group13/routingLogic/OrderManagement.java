package org.group13.routingLogic;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.group13.dataObjects.Invoice;
import org.group13.dataObjects.Order;
import org.group13.transformerBeans.ConvertToInvoiceBean;
import org.group13.transformerBeans.ConvertToOrderBean;


public class OrderManagement extends RouteBuilder{
	
	public void configure() {
        // load file orders from src/data into the JMS queue
      
        // channel with RECOGNIZED CLASSES
        // now we just have to convert it to an appropriate POJO 
        
        from("jms:csvProcessedOrders").bean(ConvertToOrderBean.class)
        .to("jms:ProcessedOrders");
        
        from("jms:ProcessedOrders")
        .wireTap("jms:OrdersTap")
        /*
        .process(new Processor() {
            public void process(Exchange exchange) throws Exception {
           	 Order order= exchange.getIn().getBody(Order.class);
           	 System.out.println("Ordered quantity "+order.getItemQuantity());
            }
        })*/;
	}
}
