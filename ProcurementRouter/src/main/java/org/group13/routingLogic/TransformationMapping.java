package org.group13.routingLogic;

import javax.jms.ConnectionFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.group13.dataObjects.Invoice;
import org.group13.dataObjects.Order;
import org.group13.transformerBeans.ConvertToInvoiceBean;
import org.group13.transformerBeans.ConvertToOrderBean;

/**
 * A Camel Java DSL Router
 */
public class TransformationMapping extends RouteBuilder {

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
             // load file orders from src/data into the JMS queue
             from("file:src/data?noop=true&delay=5000").to("jms:incomingOrders");
     
             // content-based router
             // Separate XML from CSV Orders
             // and place them in separate channels
             from("jms:incomingOrders")
             .choice()
                 .when(header("CamelFileName").endsWith(".xml"))
                     .to("jms:xmlOrders")  
                 .when(header("CamelFileName").endsWith(".csv"))
                     .to("jms:csvOrders")
             	  .otherwise()
             	  	.to("jms:badFiles").
             end();
             
             
             //check each channel, transform to POJO and conform to our standards 
             
             // first, put all test messages in a testOrder queue
             from("jms:xmlOrders")
             	.choice()
             	.when(xpath("/order[(@test)]"))
                     .to("jms:xmlTestOrders")
                .otherwise().to("jms:xmlOrders");
             
             
             // Take care of CSV orders now
             // get objects from csvlOrders channel, transform to POJO, translate
             from("jms:csvOrders").process(new Processor() {
                 public void process(Exchange exchange) throws Exception {
                     System.out.println("Received CSV order: " 
                             + exchange.getIn().getHeader("CamelFileName"));   
                 }
             })
             .unmarshal().csv()
             .split(body())
             .choice()
             	// place in appropriate queue
             	.when(body().contains("Order"))
                     .to("jms:csvProcessedOrders")  
                 .when(body().contains("Invoice"))
                     .to("jms:csvProcessedInvoices")
                   // otherwise not an order or invoice
                   // so put me in a bbad objects queue
             	  .otherwise()
             	  	.to("jms:badObjects")
             .end();
             //bean(ConvertToOrderBean.class).
             //marshal().jaxb().
             
           
             // channel with RECOGNIZED CLASSES
             // now we just have to convert it to an appropriate POJO 
             
             from("jms:csvProcessedOrders").bean(ConvertToOrderBean.class)
             .process(new Processor() {
                 public void process(Exchange exchange) throws Exception {
                	 Order order= (Order) exchange.getIn().getBody();
                	 System.out.println("Ordered quantity "+order.getItemQuantity());
                	 /* System.out.println("Converted O: "
                             + exchange.getIn().getBody());  
                             */ 
                 }
             });
             
             from("jms:csvProcessedInvoices").bean(ConvertToInvoiceBean.class)
             .process(new Processor() {
                 public void process(Exchange exchange) throws Exception {
                	 Invoice invoice= (Invoice) exchange.getIn().getBody();
                	 System.out.println("Invoice quantity "+invoice.getItemQuantity());
                 }
             });
             
             //these objects are not recognized
             from("jms:badObjects").process(new Processor() {
                 public void process(Exchange exchange) throws Exception {
                     System.out.println("Received invalid Object: " 
                             + exchange.getIn().getBody());   
                 }
             });
             
             // initial 
             from("jms:badFiles").process(new Processor() {
                 public void process(Exchange exchange) throws Exception {
                     System.out.println("Received invalid order: " 
                             + exchange.getIn().getHeader("CamelFileName"));   
                 }
             });
             
             // get objects from xmlOrders channel, transform to POJO, translate
             from("jms:xmlOrders")           
             .process(new Processor() {
                 public void process(Exchange exchange) throws Exception {
                     System.out.println("Will xml process:   " 
                             + exchange.getIn().getHeader("CamelFileName"));   
                 }
             });
             
             // get test messages from TestOrders 
             from("jms:xmlTestOrders")           
             .process(new Processor() {
                 public void process(Exchange exchange) throws Exception {
                     System.out.println("Test Order Recieved!   " 
                             + exchange.getIn().getHeader("CamelFileName"));   
                 }
             });
         }
}
