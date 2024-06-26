# Lightstreamer - "Hello World" Tutorial - Java Adapter #

<!-- START DESCRIPTION lightstreamer-example-helloworld-adapter-java -->
The demos of the "Hello World with Lightstreamer" series are very basic examples where we push the alternated strings "Hello" and "World", followed by the current timestamp, from the server to the browser. 

This project contains the source code and all the resources needed to install the Java Data Adapter for the "Hello World" Tutorial.

As an example of [Clients Using This Adapter](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-adapter-java#clients-using-this-adapter), you may refer to the [Lightstreamer - "Hello World" Tutorial - HTML Client](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-client-javascript) and view the corresponding [Live Demo](http://demos.lightstreamer.com/HelloWorld/).

## Details

Lightstreamer is made up of a Server and a set of Client libraries. Lightstreamer's job is to push real-time data over the Web in both directions (from the server to the clients and from the clients to the server). To do that, it uses a set of techniques refined and tuned over the last 13 years, including HTTP Streaming, Comet, and WebSockets.<br>
<!-- END DESCRIPTION lightstreamer-example-helloworld-adapter-java -->

Let's keep the "Hello World" demo very basic: we want to push the alternated strings "Hello" and "World", followed by the current timestamp, from the server to the browser.
The demo is based on two components: the HTML front-end (on the client side) of which full details you can find at [Lightstreamer - "Hello World" Tutorial - HTML Client](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-client-javascript), and the Data Adapter (on the server side) detailed in this project.

### Data Model

In the Lightstreamer framework, you subscribe to *Items*. An item is made up of a number of fields whose values change over time. Here are some examples of possible items:

* An item in Lightstreamer could represent an item on *eBay*, say, a pair of "Nike Air Jordan" shoes. The <b>Item name</b> would be "NIKE-AIR-JORDAN-XX3-XXIII-23-PREMIER-Limited-sz-10". Some fields would be: <i>current_bid, total_bids,</i> and <i>high_bidder</i>. When a field changes, the new value is pushed to the browser and displayed in real time.
* An item could represent a <b>weather probe</b>. The Item name would be, for example, "Mt_Everest_Probe.1" ([this probe was left by MIT](http://web.media.mit.edu/%7Efletcher/argos/weather-probes.html) after the 1998 Everest Expedition). Some fields would be: <i>temperature, barometric_pressure,</i> and <i>light_level</i>.
* In <b>finance market data dissemination</b>, an item often represents a stock quote. The item name would be, for example, "TIBX.O" (TIBCO Software Inc. on Nasdaq). Some fields would be: <i>TRDPRC_1, TRDTIM_1, BID,</i> and <i>ASK</i>.

That said, how can we represent our very complex *Hello World* messages? Of course, through an item... The item name will be `greetings`. It will have two fields: `message` and `timestamp`.

### Dig the Code

We need to create the server-side code that will pass the data to the Lightstreamer Server, which, in turn, will pass it to the front-end. This is done by writing a *Data Adapter*, a plug-in module that injects data into the Server. Let's choose *Java* to write our Data Adapter (the other current options would be to use .NET or to work at the TCP socket level, but we are adding more).

#### The Data Adapter
First, we need to implement the `SmartDataProvider` interface:

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

When the `greetings` item is subscribed to by the first user, our Adapter receives that method call and starts a thread that will generate the real-time data. If more users subscribe to the "greetings" item, the subscribe method is not called anymore. When the last user unsubscribes from this item, our Adapter is notified through the <b>unsubscribe</b> call:

```java
public void unsubscribe(String itemName)
     throws SubscriptionException, FailureException {
   if (itemName.equals("greetings") && gt != null) {
      gt.go = false;
   }
}
```

We can stop publishing data for that item. If a new user re-subscribes to <b>"greetings"</b>, the subscribe method will be called again. This approach avoids consuming processing power for items no one is currently interested in.

Now, let's see what the GreetingsThread does. Its run method is pretty straightforward:

```java
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

The full source code of this Data Adapter is shown in the `HelloWorldDataAdapter.java` source file of this project.
This example is really very basic and exploits only a minor portion of the features offered by the Lightstreamer API. To delve a bit more into the API used above, you can take a look at the online API references: [Java In-Process Adapter API Reference](https://lightstreamer.com/api/ls-adapter-inprocess/latest/index.html).

#### The Adapter Set Configuration

This Adapter Set Name is configured and will be referenced by the clients as `HELLOWORLD`.
This demo implements just the Data Adapter, while instead, as Metadata Adapter, we use the [LiteralBasedProvider](https://github.com/Lightstreamer/Lightstreamer-lib-adapter-java-inprocess#literalbasedprovider-metadata-adapter), a simple full implementation of a Metadata Adapter, already provided by Lightstreamer server.
A Metadata Adapter is responsible for managing authentication, authorization, and quality of service, but for this demo, we don't need any custom behavior.

The `adapters.xml` file for this demo should look like:
```xml
<?xml version="1.0"?>
<adapters_conf id="HELLOWORLD">
   <metadata_provider>
      <adapter_class>com.lightstreamer.adapters.metadata.LiteralBasedProvider</adapter_class>
   </metadata_provider>
   <data_provider>
      <adapter_class>HelloWorldDataAdapter</adapter_class>
   </data_provider>
</adapters_conf>
```

<i>NOTE: not all configuration options of an Adapter Set are exposed by the file suggested above. 
You can easily expand your configurations using the generic template, see the [Java In-Process Adapter Interface Project](https://github.com/Lightstreamer/Lightstreamer-lib-adapter-java-inprocess#configuration) for details.</i>

## Install

If you want to install a version of this demo on your local Lightstreamer Server, follow these steps:
* Download *Lightstreamer Server* (Lightstreamer Server comes with a free non-expiring demo license for 20 connected users) from [Lightstreamer Download page](https://lightstreamer.com/download/), and install it, as explained in the `GETTING_STARTED.TXT` file in the installation home directory.
* Get the `deploy.zip` file of the [latest release](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-adapter-java/releases), unzip it, and copy the just unzipped `HelloWorld` folder into the `adapters` folder of your Lightstreamer Server installation.
* Launch Lightstreamer Server.
* Test the Adapter, launching the [Lightstreamer - "Hello World" Tutorial - HTML Client](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-client-javascript) listed in [Clients Using This Adapter](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-adapter-java#clients-using-this-adapter).

## Build

To build your own version of this demo, instead of using the one provided in the `deploy.zip` file from the Install section above, you have two options: either use Maven (or other build tools) to take care of dependencies and building (recommended) or gather the necessary jars yourself and build it manually. For the sake of simplicity, only the Maven case is detailed here.

### Maven

You can easily build and run this application using Maven through the pom.xml file located in the root folder of this project. As an alternative, you can use an alternative build tool (e.g. Gradle, Ivy, etc.) by converting the provided pom.xml file.

Assuming Maven is installed and available in your path you can build the demo by running

```sh
 mvn package
```

## See Also 

### Clients Using This Adapter

<!-- START RELATED_ENTRIES -->

* [Lightstreamer - "Hello World" Tutorial - HTML Client](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-client-javascript)

<!-- END RELATED_ENTRIES -->

### Related Projects

* [Complete list of "Hello World" Adapter implementations with other technologies](https://github.com/Lightstreamer?utf8=%E2%9C%93&q=Lightstreamer-example-HelloWorld-adapter&type=&language=)
* [LiteralBasedProvider Metadata Adapter](https://github.com/Lightstreamer/Lightstreamer-lib-adapter-java-inprocess#literalbasedprovider-metadata-adapter)

## Lightstreamer Compatibility Notes

- Compatible with Lightstreamer Java In-Process Adapter API version 7.3.0 or newer.

## Final Notes

Please [post to our support forums](http://forums.lightstreamer.com) any feedback or questions you might have. Thanks!
