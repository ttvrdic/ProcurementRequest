package org.group13.routingLogic;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.group13.dataObjects.Invoice;
import org.group13.dataObjects.Order;
import org.group13.transformerBeans.ConvertToInvoiceBean;
import org.group13.transformerBeans.ConvertToOrderBean;

public class InvoiceManagement extends RouteBuilder{
	public void configure() {
		
		 from("jms:csvProcessedInvoices")
		 .bean(ConvertToInvoiceBean.class)
		 .to("jms:ProcessedInvoices");
         
		 from("jms:ProcessedInvoices")
		 .wireTap("jms:InvoicesTap")
	     /*   .process(new Processor() {
	            public void process(Exchange exchange) throws Exception {
	           	 Invoice invoice= exchange.getIn().getBody(Invoice.class);
	           	 System.out.println("Invoiced quantity "+invoice.getItemQuantity());
	            }
	        })*/;
		 
	}
}
