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

package org.apache.sysds.runtime.instructions.cp;

import org.apache.sysds.hops.OptimizerUtils;
import org.apache.sysds.runtime.compress.CompressedMatrixBlock;
import org.apache.sysds.runtime.controlprogram.context.ExecutionContext;
import org.apache.sysds.runtime.instructions.InstructionUtils;
import org.apache.sysds.runtime.matrix.data.MatrixBlock;
import org.apache.sysds.runtime.matrix.operators.Operator;

public class DeCompressionCPInstruction extends ComputationCPInstruction {

	private DeCompressionCPInstruction(Operator op, CPOperand in, CPOperand out, String opcode, String istr) {
		super(CPType.Compression, op, in, null, null, out, opcode, istr);
	}

	public static DeCompressionCPInstruction parseInstruction(String str) {
		String[] parts = InstructionUtils.getInstructionPartsWithValueType(str);
		String opcode = parts[0];
		CPOperand in1 = new CPOperand(parts[1]);
		CPOperand out = new CPOperand(parts[2]);
		return new DeCompressionCPInstruction(null, in1, out, opcode, str);
	}

	@Override
	public void processInstruction(ExecutionContext ec) {
		// Get matrix block input
		MatrixBlock in = ec.getMatrixInput(input1.getName());

		MatrixBlock out = (in instanceof CompressedMatrixBlock) ? 
			((CompressedMatrixBlock)in).decompress(OptimizerUtils.getConstrainedNumThreads(-1)): in;
		// MatrixBlock out = (in instanceof CompressedMatrixBlock) ? ((CompressedMatrixBlock) in)
		// 	.squeeze(OptimizerUtils.getConstrainedNumThreads(-1)) : in;
		ec.releaseMatrixInput(input1.getName());
		ec.setMatrixOutput(output.getName(), out);
	}
}
