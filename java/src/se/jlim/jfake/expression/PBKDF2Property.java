/*
 * The MIT License
 *
 * Copyright 2016 Jonas Lund.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.jlim.jfake.expression;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import se.jlim.jfake.JFake;

/**
 *
 * @author Jonas Lund
 */
public class PBKDF2Property implements Generator {

	ListExpression le;
	SecretKeyFactory skf;
	SecureRandom sr;
	
	PBKDF2Property(Generator generator) {
		try {
			if (!(generator instanceof ListExpression)) {
				throw new RuntimeException("PBKDF2Property only available on lists since they hold arguments");
			}
			le=(ListExpression) generator;
			if (le.compiled.length<2) {
				throw new RuntimeException("PBKDF2Property must have password and salt columns, optionally also an iteration count");
			}
			skf=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
			sr = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public Long size() {
		return le.compiled[0].size();
	}

	@Override
	public Object get(long idx, long seed) {
		try {
			Object saltData=le.compiled[1].get(0, seed+idx);
			if (!(saltData instanceof byte[])) {
				throw new RuntimeException("Salt for PBKDF2 must be in binary");
			}
			byte[] salt=(byte[]) saltData;
			long iter=10000;
			if (le.compiled.length>2)
				iter=(Long) le.compiled[2].get(0,seed+idx);
			String password=(String)le.compiled[0].get(idx, JFake.tumble(seed+idx));
			PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),salt,(int)iter,512);
			SecretKey secret = skf.generateSecret(spec);
			return secret.getEncoded();
		} catch (InvalidKeySpecException ex) {
			throw new RuntimeException(ex);
		}
	}
	
}
