/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysds.runtime.transform.encode;

import static org.apache.sysds.runtime.transform.encode.EncoderFactory.getEncoderType;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sysds.runtime.DMLRuntimeException;
import org.apache.sysds.runtime.matrix.data.FrameBlock;
import org.apache.sysds.runtime.matrix.data.MatrixBlock;

/**
 * Base class for all transform encoders providing both a row and block interface for decoding frames to matrices.
 *
 */
public abstract class ColumnEncoder implements Externalizable, Encoder, Comparable<ColumnEncoder> {
	protected static final Log LOG = LogFactory.getLog(ColumnEncoder.class.getName());
	private static final long serialVersionUID = 2299156350718979064L;
	protected int _colID;

	protected ColumnEncoder(int colID) {
		_colID = colID;
	}

	public abstract MatrixBlock apply(MatrixBlock in, MatrixBlock out, int outputCol);

	/**
	 * Indicates if this encoder is applicable, i.e, if there is a column to encode.
	 *
	 * @return true if a colID is set
	 */
	public boolean isApplicable() {
		return _colID != -1;
	}

	/**
	 * Indicates if this encoder is applicable for the given column ID, i.e., if it is subject to this transformation.
	 *
	 * @param colID column ID
	 * @return true if encoder is applicable for given column
	 */
	public boolean isApplicable(int colID) {
		return colID == _colID;
	}

	/**
	 * Allocates internal data structures for partial build.
	 */
	public void prepareBuildPartial() {
		// do nothing
	}

	/**
	 * Partial build of internal data structures (e.g., in distributed spark operations).
	 *
	 * @param in input frame block
	 */
	public void buildPartial(FrameBlock in) {
		// do nothing
	}

	/**
	 * Merges another encoder, of a compatible type, in after a certain position. Resizes as necessary.
	 * <code>ColumnEncoders</code> are compatible with themselves and <code>EncoderComposite</code> is compatible with
	 * every other <code>ColumnEncoders</code>. <code>MultiColumnEncoders</code> are compatible with every encoder
	 *
	 * @param other the encoder that should be merged in
	 */
	public void mergeAt(ColumnEncoder other) {
		throw new DMLRuntimeException(
			this.getClass().getSimpleName() + " does not support merging with " + other.getClass().getSimpleName());
	}

	/**
	 * Update index-ranges to after encoding. Note that only Dummycoding changes the ranges.
	 *
	 * @param beginDims begin dimensions of range
	 * @param endDims   end dimensions of range
	 */
	public void updateIndexRanges(long[] beginDims, long[] endDims, int colOffset) {
		// do nothing - default
	}

	/**
	 * Obtain the column mapping of encoded frames based on the passed meta data frame.
	 *
	 * @param meta meta data frame block
	 * @return matrix with column mapping (one row per attribute)
	 */
	public MatrixBlock getColMapping(FrameBlock meta) {
		// default: do nothing
		return null;
	}

	/**
	 * Redirects the default java serialization via externalizable to our default hadoop writable serialization for
	 * efficient broadcast/rdd serialization.
	 *
	 * @param os object output
	 * @throws IOException if IOException occurs
	 */
	@Override
	public void writeExternal(ObjectOutput os) throws IOException {
		os.writeInt(_colID);
	}

	/**
	 * Redirects the default java serialization via externalizable to our default hadoop writable serialization for
	 * efficient broadcast/rdd deserialization.
	 *
	 * @param in object input
	 * @throws IOException if IOException occur
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException {
		_colID = in.readInt();
	}

	public int getColID() {
		return _colID;
	}

	public void setColID(int colID) {
		_colID = colID;
	}

	public void shiftCol(int columnOffset) {
		_colID += columnOffset;
	}

	@Override
	public int compareTo(ColumnEncoder o) {
		return Integer.compare(getEncoderType(this), getEncoderType(o));
	}

	public enum EncoderType {
		Recode, FeatureHash, PassThrough, Bin, Dummycode, Omit, MVImpute, Composite
	}
}
