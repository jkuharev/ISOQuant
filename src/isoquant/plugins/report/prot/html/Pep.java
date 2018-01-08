/*******************************************************************************
 * THIS FILE IS PART OF ISOQUANT SOFTWARE PROJECT WRITTEN BY JOERG KUHAREV
 * 
 * Copyright (c) 2009 - 2013, JOERG KUHAREV and STEFAN TENZER
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgment:
 *    This product includes software developed by JOERG KUHAREV and STEFAN TENZER.
 * 4. Neither the name "ISOQuant" nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY JOERG KUHAREV ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL JOERG KUHAREV BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package isoquant.plugins.report.prot.html;

import java.util.Map;

public class Pep
{
	String sequence;
	String modifier;
	String type;
	int cluster_average_index;
	float ave_ref_rt;
	float ave_mass;
	int razor_count;
	int abs_count;
	String[] entries;
	Map<Integer, Long> sample_src_inten;
	Map<Integer, Long> sample_dist_inten;
	Map<Integer, Long> run_src_inten;
	Map<Integer, Long> run_dist_inten;
}
