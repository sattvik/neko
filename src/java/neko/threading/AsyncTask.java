/*
 * Copyright © 2011 Sattvik Software & Technology Resources, Ltd. Co.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * By using this software in any fashion, you are agreeing to be bound by the
 * terms of this license.  You must not remove this notice, or any other, from
 * this software.
 */
package neko.threading;

public abstract class AsyncTask<Params,Progress,Result> extends android.os.AsyncTask<Params,Progress,Result> {
    public final void superPublishProgress(Progress... values) {
        super.publishProgress(values);
    }
}
