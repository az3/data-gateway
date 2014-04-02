data-gateway
============

This is a sample Java EE Project designed to work on JBoss Application server. It contains an API for the clients to connect, and this data is then pushed to server API to be processed. The connections between servers are done asynchronously via JMS queues. The queues are durable, so if a network or electrical problem occurs on client or server hardware, they are stored locally. Message driven beans are used both on client and server side to process the transactions. To detect anomalies (such as flood, spam etc.) a fast in-memory caching system is used: Infinispan.

The project has 4 parts.
- data-gateway-lib: Core shared library.
- data-gateway-web-app: API frontend.
- data-gateway-ejb: MessageDrivenBean and TimerTask Container.
- data-gateway-ear: Packer for EJB and WAR.

They will be explained in the following section.

  **data-gateway-lib**
  ----------------
Library has main models, application constants and some helper classes for HTTP connections or such stream editing operations. In the future, data access objects (interfaces and implementations) can be placed here.

  **data-gateway-web-app**
  --------------------
The API project is the web application. Contains both the client and server servlets, so the standalone EAR can be deployed on both servers. The client entry point must be configured on the client (3rd party) application. After that, the server entry-point must be defined in the constant file (located in data-gateway-lib project). In the future, a central -and more modifyable- configuration approach can be implemented; but for the moment, they are placed as hardcoded constants.

  **data-gateway-ejb**
  ----------------
The EJB project has message driven beans, that listen to queues. 

On the client machine, only one queue is present: "queue/datagw/client/spooler". The data that is pushed to /client API is processed here. This queue is responsible for transmitting the data to the server-api.

On the server machine, there are 2 queues: "queue/datagw/server/incoming", "queue/datagw/server/processing".

The data is pushed to /server API is processed in the "incoming" queue. This queue reads the data and discards if it detects a flood. Controls for flood detection is done by the help of Infinispan, to ensure speed. The valid transactions are then sent to second queue.

The second queue is limited to 5 threads. It reads the incoming transaction data and processes it. This processing can be anything, and that part is left open for future implementations.


  **data-gateway-ear**
  ----------------
The EAR project is the main container. It packes both EJB and WAR files. This way, we have a single, standalone, compact application ready to be deployed on a JBoss application server.

  **Configuration**
  -------------
This application is tested on JBoss-AS-7.2.0.Final standalone mode, Hornetq and Infinispan enabled. 

In the Infinispan subsystem, this cache entry must be added;

<pre>
&lt;cache-container 
  name="datagwCacheContainer" 
  default-cache="incomingIpCache" 
  jndi-name="java:/infinispan/DGW-CACHE" 
  start="EAGER"&gt;
    &lt;local-cache 
      name="incomingIpCache" 
      start="LAZY" 
      jndi-name="java:/infinispan/DGW-CACHE/incomingIpCache" /&gt;
&lt;/cache-container&gt;
</pre>


For the HornetQ, these 3 queues must be added;
 
<pre>
&lt;jms-queue name="DatagwClientSpoolerQueue"&gt;
    &lt;entry name="java:/queue/datagw/client/spooler"/&gt;
    &lt;durable&gt;true&lt;/durable&gt;
&lt;/jms-queue&gt;
&lt;jms-queue name="DatagwServerIncomingQueue"&gt;
    &lt;entry name="java:/queue/datagw/server/incoming"/&gt;
    &lt;durable&gt;true&lt;/durable&gt;
&lt;/jms-queue&gt;
&lt;jms-queue name="DatagwServerProcessingQueue"&gt;
    &lt;entry name="java:/queue/datagw/server/processing"/&gt;
    &lt;durable&gt;true&lt;/durable&gt;
&lt;/jms-queue&gt;
</pre>
