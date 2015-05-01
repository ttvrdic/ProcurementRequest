package org.test.ProcurementRouter;

import javax.jms.ConnectionFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

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
             	  	.to("jms:badOrders").
             end();
             
             //check each channel, transform to POJO and conform to our standards 
             // First of all, check test messages and log them being ignored
            /* from("jms:xmlOrders").filter(xpath("/order[(@test)]"))           
             .process(new Processor() {
                 public void process(Exchange exchange) throws Exception {
                     System.out.println("Test message, will be ignored:   " 
                             + exchange.getIn().getHeader("CamelFileName"));   
                 }
             });*/
             
             //check each channel, transform to POJO and conform to our standards 
             // first, put all test messages in a testOrder queue
             from("jms:xmlOrders")
             	.choice()
             	.when(xpath("/order[(@test)]"))
                     .to("jms:xmlTestOrders")
                .otherwise().to("jms:xmlOrders");
             
             // get objects from xmlOrders channel, transform to POJO, translate
             from("jms:xmlOrders")           
             .process(new Processor() {
                 public void process(Exchange exchange) throws Exception {
                     System.out.println("Will process:   " 
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
             // get objects from xmlOrders channel, transform to POJO, translate
             from("jms:csvOrders").process(new Processor() {
                 public void process(Exchange exchange) throws Exception {
                     System.out.println("Received CSV order: " 
                             + exchange.getIn().getHeader("CamelFileName"));   
                 }
             });
             
             //these objects are not recognized
             // put them in a badOrders queue
             from("jms:badOrders").process(new Processor() {
                 public void process(Exchange exchange) throws Exception {
                     System.out.println("Received invalid order: " 
                             + exchange.getIn().getHeader("CamelFileName"));   
                 }
             });
         }
}
