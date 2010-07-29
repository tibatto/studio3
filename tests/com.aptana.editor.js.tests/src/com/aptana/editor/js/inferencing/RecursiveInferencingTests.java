package com.aptana.editor.js.inferencing;


public class RecursiveInferencingTests extends InferencingTestsBase
{
	/**
	 * testIdentifierCycle
	 */
	public void testIdentifierCycle()
	{
		this.varTypeTests("var a = b, b = a;", "a");
		this.varTypeTests("var a = b, b = a;", "b");
	}

	/**
	 * testIdentifierCycle2
	 */
	public void testIdentifierCycle2()
	{
		this.varTypeTests("var a = b, b = c, c = a;", "a");
		this.varTypeTests("var a = b, b = c, c = a;", "b");
		this.varTypeTests("var a = b, b = c, c = a;", "c");
	}

	/**
	 * testInvocationCycle
	 */
	public void testInvocationCycle()
	{
		String source = this.getContent("inferencing/invocation-cycle-1.js");

		this.lastStatementTypeTests(source, "Function");
	}

	/**
	 * testInvocationCycle2
	 */
	public void testInvocationCycle2()
	{
		String source = this.getContent("inferencing/invocation-cycle-2.js");

		this.lastStatementTypeTests(source, "Number");
	}

	/**
	 * testInvocationCycle3
	 */
	public void testInvocationCycle3()
	{
		String source = this.getContent("inferencing/invocation-cycle-3.js");

		this.lastStatementTypeTests(source, "Number");
	}

	/**
	 * testObjectPropertyReturnsSelf
	 */
	public void testObjectPropertyReturnsSelf()
	{
		String source = this.getContent("inferencing/chaining.js");

		this.lastStatementTypeTests(source, "Utils.create.self");
	}
}
