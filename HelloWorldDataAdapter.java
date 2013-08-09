/*
 * Copyright 2013 Weswit Srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.*;
import java.io.File;
import com.lightstreamer.interfaces.data.*;

public class HelloWorldDataAdapter implements SmartDataProvider {

    private ItemEventListener listener;
    private volatile GreetingsThread gt;

    public void init(Map params, File configDir) throws DataProviderException {
    }

    public boolean isSnapshotAvailable(String itemName) throws SubscriptionException {
        return false;
    }

    public void setListener(ItemEventListener listener) {
        this.listener = listener;
    }

    public void subscribe(String itemName, Object itemHandle, boolean needsIterator)
            throws SubscriptionException, FailureException {
        if (itemName.equals("greetings")) {
            gt = new GreetingsThread(itemHandle);
            gt.start();
        }
    }
    
    public void subscribe(String itemName, boolean needsIterator)
                throws SubscriptionException, FailureException {
    }         	

    public void unsubscribe(String itemName) throws SubscriptionException,
            FailureException {
        if (itemName.equals("greetings") && gt != null) {
            gt.go = false;
        }
    }

    class GreetingsThread extends Thread {

        private final Object itemHandle;
        public volatile boolean go = true;

        public GreetingsThread(Object itemHandle) {
            this.itemHandle = itemHandle;
        }

        public void run() {
		        int c = 0;
    		    Random rand = new Random();
            while(go) {
                Map<String, String> data = new HashMap<String, String>();
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
    }

}