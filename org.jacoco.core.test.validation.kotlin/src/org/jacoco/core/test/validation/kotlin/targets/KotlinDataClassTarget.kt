/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin.targets

// method("C.<init>(Ljava/lang/String;Ljava/lang/String;)V")
data class C( // assertFullyCovered()

        // method("C.getX()Ljava/lang/String;")
        val x: String, // assertNotCovered()

        // method("C.getY()Ljava/lang/String;")
        // method("C.setY(Ljava/lang/String;)V")
        var y: String // assertNotCovered()

) // assertEmpty()

// method("KotlinDataClassTargetKt.main([Ljava/lang/String;)V")
fun main(args: Array<String>) {
    C("", "")
}
