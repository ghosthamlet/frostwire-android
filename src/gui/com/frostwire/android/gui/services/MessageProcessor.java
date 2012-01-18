/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.android.gui.services;

import com.frostwire.android.core.Constants;
import com.frostwire.android.core.messages.FrostWireMessage;
import com.frostwire.android.core.messages.PingMessage;
import com.frostwire.android.gui.PeerManager;
import com.frostwire.android.util.concurrent.ThreadPool;

/**
 * A Universal Message processor responsible of handing over messages to different Engine Managers.
 * These Engine Managers should implement Subject, so that UI Activities can Observe them.
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class MessageProcessor extends QueueProcessor<FrostWireMessage> {

    public static String TAG = "FW.MessageProcessor";

    public MessageProcessor(ThreadPool pool) {
        super("MessageProcessor", pool, Constants.MESSAGE_PROCESSOR_CAPACITY);
    }

    @Override
    public void processElement(FrostWireMessage elem) throws Exception {
        if (elem == null) {
            return;
        }

        byte type = elem.getType();

        if (type == Constants.MESSAGE_TYPE_PING) {
            processPingMessage((PingMessage) elem);
        }
    }

    private void processPingMessage(PingMessage elem) {
        PeerManager.instance().onMessageReceived(elem);
    }
}
