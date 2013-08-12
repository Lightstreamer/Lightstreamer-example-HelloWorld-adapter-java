# Lightstreamer "Hello World" Adapter for Java #

Lightstreamer is made up of a Server and a set of Client libraries. Lightstreamer's job is to push real-time data over the Web in both directions (from the server to the clients and from the clients to the server). To do that, it uses a set of techniques refined and tuned over the last 13 years, including HTTP Streaming, Comet, and WebSockets.<br>

Let's see how to build a "Hello World" application with Lightstreamer. The client will be based on <b>HTML</b> and <b>JavaScript</b>, while the server-side Data Adapter will be based on <b>Java</b>.<br>

This project focuses on the server-side Adapter.

## What do we want our application to do? ##

Let's keep the application very basic. We want to push the alternated strings "Hello" and "World", followed by the current timestamp, from the server to the browser. Yes, a very exciting application :-)

## What data model should we use? ##

In the Lightstreamer framework, you subscribe to <b>Items</b>. An item is made up of a number of fields whose values change over time. Here are some examples of possible items:

* An item in Lightstreamer could represent an item on <b>eBay</b>, say, a pair of "Nike Air Jordan" shoes. The <b>Item name</b> would be "NIKE-AIR-JORDAN-XX3-XXIII-23-PREMIER-Limited-sz-10". Some fields would be: <i>current_bid, total_bids,</i> and <i>high_bidder</i>. When a field changes, the new value is pushed to the browser and displayed in real time.
* An item could represent a <b>weather probe</b>. The Item name would be, for example, "Mt_Everest_Probe.1" ([this probe was left by MIT](http://web.media.mit.edu/%7Efletcher/argos/weather-probes.html) after the 1998 Everest Expedition). Some fields would be: <i>temperature, barometric_pressure,</i> and <i>light_level</i>.
* In <b>finance market data dissemination</b>, an item often represents a stock quote. The item name would be, for example, "TIBX.O" (TIBCO Software Inc. on Nasdaq). Some fields would be: <i>TRDPRC_1, TRDTIM_1, BID,</i> and <i>ASK</i>.

That said, how can we represent our very complex <b>Hello World</b> messages? Of course through an item... The item name will be <b>"greetings"</b>. It will have two fields: <i>message</i> and <i>timestamp</i>.

## Let's get started ##

First, [download and install Lightstreamer Colosseo](http://www.lightstreamer.com/download). After you see some of the pre-installed demos running, you will be sure that the Server is ready to host our application.<br>

Now we need to develop two components: the HTML front-end (on the client side) of which fully details you can find [here](https://github.com/Weswit/Lightstreamer-example-HelloWorld-client-javascript), and the Data Adapter (on the server side) detailed in this project.

## Creating the Data Adapter ##

We need to create the server-side code that will pass the data to the Lightstreamer Server, which in turn will pass it to the front-end. This is done by writing a <b>Data Adapter</b>, a plug-in module that injects data into the Server. Let’s choose <b>Java</b> to write our Data Adapter (the other current options would be to use .NET or to work at the TCP socket level, but we are adding more).
First, we need to implement the <b>SmartDataProvider</b> interface:

```java
public class HelloWorldDataAdapter implements SmartDataProvider {
```

We will be passed a reference to a <b>listener</b> that we will use to inject the real-time events:

```java
private ItemEventListener listener;

public void setListener(ItemEventListener listener) {
   this.listener = listener;
}
```

Then we must be ready to accept <b>subscription</b> requests:

```java
public void subscribe(String itemName, Object itemHandle, boolean needsIterator)
     throws SubscriptionException, FailureException {
   if (itemName.equals("greetings")) {
      gt = new GreetingsThread(itemHandle);
      gt.start();
   }
}
```

When the "greetings" item is subscribed to by the first user, our Adapter receives that method call and starts a thread that will generate the real-time data. If more users subscribe to the "greetings" item, the subscribe method is not called anymore. When the last user unsubscribes from this item, our Adapter is notified through the <b>unsubscribe</b> call:

```java
public void unsubscribe(String itemName)
     throws SubscriptionException, FailureException {
   if (itemName.equals("greetings") && gt != null) {
      gt.go = false;
   }
}
```

We can stop publishing data for that item. If a new user re-subscribes to <b>"greetings"</b>, the subscribe method wiull be called again. This approach avoids consuming processing power for items nobody is currently interested in.

Now, let's see what the GreetingsThread does. Its run method is pretty straightforward:

```html
public void run() {
   int c = 0;
   Random rand = new Random();
   while(go) {
      Map data = new HashMap();
      data.put("message", c % 2 == 0 ? "Hello" : "World");
      data.put("timestamp", new Date().toString());
      listener.smartUpdate(itemHandle, data, false);
      c++;
      try {
         Thread.sleep(1000 + rand.nextInt(2000));
      } catch (InterruptedException e) {
      }
   }
}
```

We create a HashMap containing the message (alternating "Hello" and "World") and the current timestamp. Then we inject the HashMap into the Lightstreamer Server through the listener (and the itemHandle we were passed at subscription time). We do a random pause between 1 and 3 seconds, and we are ready to generate a new event.

The full source code of this Data Adapter is shown in the HelloWorldDataAdapter.java source file of this project.

## Let's deploy the Adapter ##

We should now <b>compile</b> the Data Adapter and plug it into the Lightstreamer Server. To compile it, we need to include the <b>Adapter interface</b> in the Java compiler classpath. We can find this interface in the ls-adapter-interface.jar file located in "Lightstreamer/DOCS-SDKs/sdk_adapter_java/lib" within your installation. If compiling succeeds, you will get two classes:

- HelloWorldDataAdapter$GreetingsThread.class
- HelloWorldDataAdapter.class

To <b>deploy</b> a Data Adapter, we need to create a folder directly under "Lightstreamer/adapters". Let's call it "HelloWorld". Create a "classes" folder inside "HelloWorld" and put the two .class files in it.

The final step is to create a <b>deployment descriptor</b> for this Adapter. This file should be called "adapters.xml" and put in the "HelloWorld" folder. Its content is very simple:

```xml
<?xml version="1.0"?>
<adapters_conf id="HELLOWORLD">
   <metadata_provider>
      <adapter_class>
         com.lightstreamer.adapters.metadata.LiteralBasedProvider
      </adapter_class>
   </metadata_provider>
   <data_provider>
      <adapter_class>
         HelloWorldDataAdapter
      </adapter_class>
   </data_provider>
</adapters_conf>
```

We assign an <b>ID</b> to our Adapter: <b>"HELLOWORLD"</b>. This is the same that we used in the setAdapterName method of the client. Then, we define a default Metadata Adapter (a Metadata Adapter is responsible for managing authentication, authorization, and quality of service; we don't need any custom behavior for our application). And we define the main class of our brand new Data Adapter.<br>
An example of deploy directory of this Data Adapter is shown in the "deploy.zip" file of [latest release](https://github.com/Weswit/Lightstreamer-example-HelloWorld-adapter-java/releases) of this project.

## Ready to go ##

Please, in order to test this adapter follow the steps in [this tutorial](https://github.com/Weswit/Lightstreamer-example-HelloWorld-client-javascript).

## Final notes ##

This example is really very basic and exploits only a minor portion of the features offered by the Lightstreamer API. To delve a bit more into the API used above you can take a look at the online API references: [Java Adapter API Reference](http://www.lightstreamer.com/docs/adapter_java_api/index.html).

Please [post to our support forums](forums.lightstreamer.com) any feedback or question you might have. Thanks!

# See Also #

## Clients using this Adapter ##

* ["Hello World" with Lightstreamer Colosseo](https://github.com/Weswit/Lightstreamer-example-HelloWorld-client-javascript)

# Lightstreamer Compatibility Notes #

- Compatible with Lightstreamer Java Adapter API version 5.1 or newer.