/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Authors
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Created by Orange / Date - 2020/04/27 - for the STOP-COVID project
 */

package com.orange.proximitynotification.ble.advertiser

import com.orange.proximitynotification.ble.BleSettings

interface BleAdvertiser {
    val settings: BleSettings

    interface Callback {
        fun onError(errorCode: Int)
    }

    fun start(data: ByteArray, callback: Callback)
    fun stop()
}