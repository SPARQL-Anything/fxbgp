package io.github.sparqlanything.fxbgp;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;

public class AnnotationFactoryTest extends BGPTestAbstract {

	protected FXAnnotationFactory IF;

	@Rule
	public TestName name = new TestName();

	public AnnotationFactoryTest() {
		super(FXModel.getFXModel());
	}

	@Override
	public void before() {
		super.before();
		IF = IF();
	}

	@Test
	public void testIF1() throws IOException {
		readBGP(name.getMethodName().substring(4));
		FXBGPAnnotation ibgp = IF.make(new OpBGP(bp()));

		Assert.assertTrue(ibgp.isStart());
		Assert.assertFalse(ibgp.isGrounded());

		Assert.assertTrue(ibgp.getannotationOfNodes().size() == 3);
	}

	@Test
	public void testIF2() throws IOException {
		readBGP(name.getMethodName().substring(4));
		FXBGPAnnotation ibgp = IF.make(new OpBGP(bp()));

		Assert.assertTrue(ibgp.isStart());
		Assert.assertFalse(ibgp.isGrounded());
		Assert.assertTrue(ibgp.getannotationOfNodes().size() == 5);

		// Let's interpret the first subject as a container
		Node s = ibgp.getOpBGP().getPattern().getList().iterator().next().getSubject();
		FXBGPAnnotation ibgp2 = IF.make(ibgp, s, FX.Container);
		Assert.assertFalse(ibgp2.isStart());
		Assert.assertEquals(ibgp, ibgp2.previous());
	}


	@Test
	public void equalsOfBGP() throws IOException {
		readBGP("IF1");
		FXBGPAnnotation ibgp = IF.make(new OpBGP(bp()));
		FXBGPAnnotation ibgp2 = IF.make(new OpBGP(bp()));
		Assert.assertEquals(ibgp, ibgp2);
	}


	@Test
	public void equalsOfNode() throws IOException {
		readBGP("IF1");
		FXNodeAnnotation in = IF.make(new OpBGP(bp()), new OpBGP(bp()).getPattern().getList().get(0).getSubject(), FX.Container);
		FXNodeAnnotation in2 = IF.make(new OpBGP(bp()), new OpBGP(bp()).getPattern().getList().get(0).getSubject(), FX.Container);
		Assert.assertEquals(in, in2);
	}

	@Test
	public void hashcodeOfBGP() throws IOException {
		readBGP("IF1");
		FXBGPAnnotation ibgp = IF.make(new OpBGP(bp()));
		FXBGPAnnotation ibgp2 = IF.make(new OpBGP(bp()));
		Assert.assertEquals(ibgp.hashCode(), ibgp2.hashCode());
	}


	@Test
	public void hashcodeOfNode() throws IOException {
		readBGP("IF1");
		FXNodeAnnotation in = IF.make(new OpBGP(bp()), new OpBGP(bp()).getPattern().getList().get(0).getSubject(), FX.Container);
		FXNodeAnnotation in2 = IF.make(new OpBGP(bp()), new OpBGP(bp()).getPattern().getList().get(0).getSubject(), FX.Container);
		Assert.assertEquals(in, in2);
	}


}
