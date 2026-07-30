/*
 * Copyright (c) Lightstreamer Srl
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

package com.lightstreamer.example_helloworld_adapter_java.adapters;

import java.util.*;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.lightstreamer.adapter.common.InitException;
import com.lightstreamer.adapter.common.Mode;
import com.lightstreamer.adapter.data.DataAdapter;
import com.lightstreamer.adapter.data.ItemListener;
import com.lightstreamer.adapter.data.ItemManager;
import com.lightstreamer.adapter.data.SubscriptionException;

public class HelloWorldDataAdapter implements DataAdapter {

    private volatile GreetingsThread gt;

    public void init(Map<String, String> params, File configDir) throws InitException {
    }

    @Override
    public CompletionStage<ItemManager> getItemManager(String itemName) throws SubscriptionException {
        return CompletableFuture.completedFuture(new HelloWorldItemManager(itemName));
    }

    @Override
    public void shutdown() {
    }

    private class HelloWorldItemManager implements ItemManager {

        private final String itemName;
        private volatile ItemListener listener;

        HelloWorldItemManager(String itemName) {
            this.itemName = itemName;
        }

        @Override
        public Mode getSupportedMode() {
            return Mode.MERGE;
        }

        @Override
        public CompletionStage<Void> subscribe(ItemListener listener) throws SubscriptionException {
            this.listener = listener;
            if (itemName.equals("greetings")) {
                gt = new GreetingsThread(listener);
                gt.start();
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public boolean isSnapshotAvailable() {
            return false;
        }

        @Override
        public CompletionStage<Void> unsubscribe() throws SubscriptionException {
            if (itemName.equals("greetings") && gt != null) {
                gt.go = false;
            }
            this.listener = null;
            return CompletableFuture.completedFuture(null);
        }
    }

    class GreetingsThread extends Thread {

        private final ItemListener listener;
        public volatile boolean go = true;

        public GreetingsThread(ItemListener listener) {
            this.listener = listener;
        }

        public void run() {
            int c = 0;
            Random rand = new Random();
            while(go) {
                Map<String, String> data = new HashMap<String, String>();
                data.put("message", c % 2 == 0 ? "Hello" : "World");
                data.put("timestamp", new Date().toString());
                listener.update(data);
                c++;
                try {
                    Thread.sleep(1000 + rand.nextInt(2000));
                } catch (InterruptedException e) {
                }
            }
        }
    }

}