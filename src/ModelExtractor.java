
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Statement;


public class ModelExtractor {
	private static ModelExtractor modelExtractor;
	private ModelExtractor() {
		
	}
	
	public static ModelExtractor createModelExtractor() {
		if(modelExtractor == null){
			modelExtractor = new ModelExtractor();
		}

		return modelExtractor;
	}
	
	public HashMap<Integer,Integer> getGenealogyContext(ASTNode node) {		
		HashMap<Integer,Integer> genealogy = new HashMap<Integer,Integer>();
//		System.out.println("NODE!!!!!!!!!!!!!!!!!!!!!");
//		System.out.println(node.toString().replaceAll("[\\t\\n\\r,]+"," ") + " " +node.getNodeType());

		ASTNode currentNode = node;
		while(currentNode!= null && currentNode.getNodeType()!=ASTNode.METHOD_DECLARATION) {
			if(currentNode.getNodeType()!=ASTNode.BLOCK) {
//				System.out.println("anchestor");
				
				int type = currentNode.getNodeType();//
//				System.out.println(type+ " "+ this.getNodeType(currentNode));
//				System.out.println(currentNode.toString().replaceAll("[\\t\\n\\r,]+"," ") + " "+type);
				if(genealogy.containsKey(type)) {
					genealogy.put(type, genealogy.get(type)+1);
				}
				else {
					genealogy.put(type, 1);
				}
			}
			currentNode = currentNode.getParent();
		}
		currentNode = node.getParent();
		while(currentNode!= null && currentNode.getNodeType()!=ASTNode.BLOCK) {
			currentNode = currentNode.getParent();
		}
		
		if(currentNode!= null) {
//			System.out.println("BLOCK "+ currentNode.toString());
			currentNode.accept(new ASTVisitor() {
				@Override
				public void preVisit(ASTNode child) {
//					if(child.equals(temporaryNode)) {
//												System.out.println("SAME SAME");
						//						System.out.println(node);
//						return;
//					}
					
					if(child instanceof Expression) {
//						System.out.println("EXP");
						int type = child.getNodeType();
//						System.out.println(type+ " "+ getNodeType(child));
						if(genealogy.containsKey(type)) {
							genealogy.put(type, genealogy.get(type)+1);
						}
						else {
							genealogy.put(type, 1);
						}
//						System.out.println(child.toString().replaceAll("[\\t\\n\\r,]+"," ") + " "+type);
					}

					else if(child instanceof Statement) {
//						System.out.println("STMT");
						int type = child.getNodeType();
//						System.out.println(type+ " "+ getNodeType(child));
						if(type==ASTNode.BLOCK) {
							return;
						}
						else if(genealogy.containsKey(type)) {
							genealogy.put(type, genealogy.get(type)+1);
						}
						else {
							genealogy.put(type, 1);
						}
//						System.out.println(child.toString().replaceAll("[\\t\\n\\r,]+"," ") + " "+type);
					}
				}
			});
		}

		return genealogy;
	}
	
	public String getContext(Node node, int threshold) {
//		System.out.println("Node: " +node);
		String context = node.toString();
		
		//return expression
		if((node.endLine-node.startLine+1)>=threshold) {
//			System.out.println("Context1 "+context);
			return context;
		}
		ASTNode currentNode = node.node.getParent();

		//find parent statement
		while(currentNode instanceof Statement==false && currentNode!=null) {
			currentNode = currentNode.getParent();
//			System.out.println("Parent "+currentNode + " "+(currentNode instanceof Statement));
		}
		if(currentNode!=null) {
//			System.out.println("Final Parent "+currentNode);
			ASTNode parentStatement = currentNode;
			context = currentNode.toString();
			PatchGenerator patchGenerator = PatchGenerator.createPatchGenerator();
			int startLine = patchGenerator.compilationUnit.getLineNumber(currentNode.getStartPosition());
			int endLine = patchGenerator.compilationUnit.getLineNumber(currentNode.getStartPosition()+currentNode.getLength());
			
			//return parent statement
			if((endLine-startLine+1)>=threshold) {
//				System.out.println("Context2 "+context);
				return context;
			}
			
			//find grandparent statement
			currentNode = parentStatement.getParent();
			while(currentNode instanceof Statement==false && currentNode!=null) {
				currentNode = currentNode.getParent();
			}
			if(currentNode!=null) {
				startLine = patchGenerator.compilationUnit.getLineNumber(currentNode.getStartPosition());
				endLine = patchGenerator.compilationUnit.getLineNumber(currentNode.getStartPosition()+currentNode.getLength());
				if((endLine-startLine+1)<=threshold) {
					context = currentNode.toString();
//					System.out.println("Context3 "+context);
					return context;
				}
			}
			currentNode = parentStatement.getParent();
			while(currentNode!= null && currentNode.getNodeType()!=ASTNode.BLOCK) {
				currentNode = currentNode.getParent();
			}
			Block block = (Block) currentNode;
//			System.out.println("BLOCK "+block.statements());
			int length = block.statements().size();
			int index = -1;
			for(int i=0; i<length;i++) {
				if(block.statements().get(i).toString().equals(parentStatement.toString())) {
//					System.out.println("Match "+i);
					index = i;
					break;
				}
			}
			
			if(index==-1) {
				for(int i=0; i<length;i++) {
					ASTNode statement = (ASTNode) block.statements().get(i);
					if(statement.toString().contains(parentStatement.toString())) {
//						System.out.println("Substring "+i);
						startLine = patchGenerator.compilationUnit.getLineNumber(statement.getStartPosition());
						endLine = patchGenerator.compilationUnit.getLineNumber(statement.getStartPosition()+statement.getLength());
						if((endLine-startLine+1)<=threshold) {
							context = statement.toString();
//							System.out.println("Context4 "+context);
						}
						else {
//							System.out.println("Context5 "+context);
							return context;
						}
					}
				}
			}
			
			else if(index>0 && index<length-1) {
				ASTNode statement1 = (ASTNode) block.statements().get(index-1);
				ASTNode statement =  (ASTNode) block.statements().get(index);
				ASTNode statement2 = (ASTNode) block.statements().get(index+1);
				
				startLine = patchGenerator.compilationUnit.getLineNumber(statement1.getStartPosition())+patchGenerator.compilationUnit.getLineNumber(statement.getStartPosition())+patchGenerator.compilationUnit.getLineNumber(statement2.getStartPosition());
				endLine = patchGenerator.compilationUnit.getLineNumber(statement1.getStartPosition()+statement1.getLength())+patchGenerator.compilationUnit.getLineNumber(statement.getStartPosition()+statement.getLength())+patchGenerator.compilationUnit.getLineNumber(statement2.getStartPosition()+statement2.getLength());
				if((endLine-startLine+3)<=threshold) {
					context = statement1.toString() + statement.toString() + statement2.toString();
//					System.out.println("Context6 "+context);
					return context;
				}
			}
			
			else if(index>0) {
				ASTNode statement1 = (ASTNode) block.statements().get(index-1);
				ASTNode statement =  (ASTNode) block.statements().get(index);
				
				startLine = patchGenerator.compilationUnit.getLineNumber(statement1.getStartPosition())+patchGenerator.compilationUnit.getLineNumber(statement.getStartPosition());
				endLine = patchGenerator.compilationUnit.getLineNumber(statement1.getStartPosition()+statement1.getLength())+patchGenerator.compilationUnit.getLineNumber(statement.getStartPosition()+statement.getLength());
				if((endLine-startLine+2)<=threshold) {
					context = statement1.toString() + statement.toString();
//					System.out.println("Context7 "+context);
					return context;
				}
			}
			
			else if(index<length-1) {
				ASTNode statement =  (ASTNode) block.statements().get(index);
				ASTNode statement2 = (ASTNode) block.statements().get(index+1);
				
				startLine = patchGenerator.compilationUnit.getLineNumber(statement.getStartPosition())+patchGenerator.compilationUnit.getLineNumber(statement2.getStartPosition());
				endLine = patchGenerator.compilationUnit.getLineNumber(statement.getStartPosition()+statement.getLength())+patchGenerator.compilationUnit.getLineNumber(statement2.getStartPosition()+statement2.getLength());
				if((endLine-startLine+2)<=threshold) {
					context = statement.toString() + statement2.toString();
//					System.out.println("Context8 "+context);
					return context;
				}
			}
			
		}
		
//		System.out.println("Context9 "+context);
		return context;
	}

	public double getGenealogySimilarityScore(HashMap<Integer,Integer>genealogyTarget, HashMap<Integer,Integer>genealogySource) {
		double total1 = 0;
		double total2 = 0;
		if(genealogyTarget.isEmpty() || genealogySource.isEmpty()) {
			return total1;
		}
		HashSet<Integer> commonKeys = new HashSet<Integer>(genealogyTarget.keySet());
		commonKeys.retainAll(genealogySource.keySet());

		Iterator<Integer> iterator = commonKeys.iterator();  
		while(iterator.hasNext()){  
			Integer type = iterator.next();
//			System.out.println(type+ ": "+genealogyTarget.get(type) + " " +genealogySource.get(type));
			total1 = total1 + Integer.min(genealogyTarget.get(type), genealogySource.get(type));
		}
		
		HashSet<Integer> targetKeys = new HashSet<Integer>(genealogyTarget.keySet());
		iterator = targetKeys.iterator();  
		while(iterator.hasNext()) {  
			Integer type = iterator.next();
//			System.out.println(type + " "+genealogyTarget.get(type));
			total2 = total2 + genealogyTarget.get(type);
		}
		
		//		System.out.println(genealogyTarget.size());

		return total1/total2;
	}

	public HashSet<Variable> getVariableContext(ASTNode node) {
		HashSet<Variable> variableAccessed = new HashSet<Variable>();
//		System.out.println("\nNODE!!!!!!!!!!!!!!!!!!!!!!!!!");
//		System.out.println(node);
		node.accept(new ASTVisitor() {
			@Override
			public void preVisit(ASTNode child) {
				if(child.getNodeType()==ASTNode.SIMPLE_NAME) {
					IBinding binding = (IBinding) ((Name) child).resolveBinding();
					if(binding!=null && binding.getKind()==IBinding.VARIABLE) {
						Variable variable = new Variable();
						variable.name = binding.getName();
						
						if((((Name) child).resolveTypeBinding())!=null) {
							variable.type = (((Name) child).resolveTypeBinding()).getQualifiedName();
						}
						else {
							variable.type = "";
						}
						
						Iterator<Variable> iterator = variableAccessed.iterator(); 
						boolean match = false;
						while(iterator.hasNext())  
						{ 
							Variable currentVariable = iterator.next(); 
							if(currentVariable.name.equals(variable.name) && currentVariable.type.equals(variable.type)) {
								match = true;
								break;
							}
						}
						if(match == false) {
//							System.out.println(variable);
							variableAccessed.add(variable);
						}
					}
					
				}
			}
		});
//		System.out.println(variableAccessed);
		return variableAccessed;
	}

	public double getVariableSimilarityScore(HashSet<Variable> variableAccessedTarget, HashSet<Variable> variableAccessedSource) {
		int match = 0;
//		System.out.println("F " +variableAccessedTarget.size());
//		System.out.print(variableAccessedSource.size());
		if(variableAccessedTarget.isEmpty() || variableAccessedSource.isEmpty()) {
//			System.out.println("EMPTY VAR");
			return match;
		}
		
		Iterator<Variable> iteratorTarget = variableAccessedTarget.iterator();  
		while(iteratorTarget.hasNext())  
		{  
			Variable variableTarget = iteratorTarget.next();  
			Iterator<Variable> iteratorSource = variableAccessedSource.iterator();  
			while(iteratorSource.hasNext())  
			{
				Variable variableSource = iteratorSource.next();
				if(variableSource.name.equals(variableTarget.name) && variableSource.type.equals(variableTarget.type)) {
//					System.out.println("VAR " +variableSource.name);
					match++;
					break;
				}
			}
		}  
		
//		System.out.println("SIZE " +variableAccessedSource.size()+ " " +variableAccessedTarget.size()+ " "+match);
		
		return ((double)((double)match/(double)(variableAccessedSource.size()+variableAccessedTarget.size()-match)));//*variableAccessedSource.size());
	}
	
	public double getNormalizedLongestCommonSubsequence(String faultyNode, String fixingIngredient) {
//		System.out.println("faultyNodeContext "+faultyNodeContext);
//		System.out.println("fixingIngredientContext "+fixingIngredientContext);
		return getLongestCommonSubsequence(faultyNode, fixingIngredient)/(double)Math.max(faultyNode.length(), fixingIngredient.length());
	}
	
	private int getLongestCommonSubsequence(String faultyNode, String fixingIngredient) {
		int m = faultyNode.length();
		int n = fixingIngredient.length();
		int[][] dp = new int[m+1][n+1];
	 
		for(int i=0; i<=m; i++){
			for(int j=0; j<=n; j++){
				if(i==0 || j==0){
					dp[i][j]=0;
				}else if(faultyNode.charAt(i-1)==fixingIngredient.charAt(j-1)){
					dp[i][j] = 1 + dp[i-1][j-1];
				}else{
					dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
				}
			}
		}
	 
		return dp[m][n];
	}
	
	public double getTokenSimilarityScore(HashMap<String,Integer> targetTokens, HashMap<String,Integer> sourceTokens) {
//		System.out.println(targetTokens.size()+" " +sourceTokens.size());
		HashMap<String,Integer> result = new HashMap<String,Integer>(targetTokens);
		result.keySet().retainAll(sourceTokens.keySet());

//		System.out.println(result.keySet());
//		System.out.println();
		return (double)result.size()/(double)(targetTokens.size()+sourceTokens.size()-result.size());
	}

	public String getNodeType(ASTNode node) {
		if(node.getNodeType()==ASTNode.ANNOTATION_TYPE_DECLARATION) {
			return "ANNOTATION_TYPE_DECLARATION";
		}
		if(node.getNodeType()==ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION) {
			return "ANNOTATION_TYPE_MEMBER_DECLARATION";
		}
		if(node.getNodeType()==ASTNode.ANONYMOUS_CLASS_DECLARATION) {
			return "ANONYMOUS_CLASS_DECLARATION";
		}
		if(node.getNodeType()==ASTNode.ARRAY_ACCESS) {
			return "ARRAY_ACCESS";
		}
		if(node.getNodeType()==ASTNode.ARRAY_CREATION) {
			return "ARRAY_CREATION";
		}
		if(node.getNodeType()==ASTNode.ARRAY_INITIALIZER) {
			return "ARRAY_INITIALIZER";
		}
		if(node.getNodeType()==ASTNode.ARRAY_TYPE) {
			return "ARRAY_TYPE";
		}
		if(node.getNodeType()==ASTNode.ASSERT_STATEMENT) {
			return "ASSERT_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.ASSIGNMENT) {
			return "ASSIGNMENT";
		}
		
		if(node.getNodeType()==ASTNode.BLOCK) {
			return "BLOCK";
		}
		if(node.getNodeType()==ASTNode.BLOCK_COMMENT) {
			return "ANNOTATION_TYPE_DECLARATION";
		}
		if(node.getNodeType()==ASTNode.BOOLEAN_LITERAL) {
			return "BOOLEAN_LITERAL";
		}
		if(node.getNodeType()==ASTNode.BREAK_STATEMENT) {
			return "BREAK_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.CAST_EXPRESSION) {
			return "CAST_EXPRESSION";
		}
		if(node.getNodeType()==ASTNode.CATCH_CLAUSE) {
			return "CATCH_CLAUSE";
		}
		if(node.getNodeType()==ASTNode.CHARACTER_LITERAL) {
			return "CHARACTER_LITERAL";
		}
		if(node.getNodeType()==ASTNode.CLASS_INSTANCE_CREATION) {
			return "CLASS_INSTANCE_CREATION";
		}
		if(node.getNodeType()==ASTNode.COMPILATION_UNIT) {
			return "COMPILATION_UNIT";
		}
		if(node.getNodeType()==ASTNode.CONDITIONAL_EXPRESSION) {
			return "CONDITIONAL_EXPRESSION";
		}
		if(node.getNodeType()==ASTNode.CONSTRUCTOR_INVOCATION) {
			return "CONSTRUCTOR_INVOCATION";
		}
		if(node.getNodeType()==ASTNode.CONTINUE_STATEMENT) {
			return "CONTINUE_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.CREATION_REFERENCE) {
			return "CREATION_REFERENCE";
		}
		if(node.getNodeType()==ASTNode.DIMENSION) {
			return "DIMENSION";
		}
		if(node.getNodeType()==ASTNode.DO_STATEMENT) {
			return "DO_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.EMPTY_STATEMENT) {
			return "EMPTY_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.ENHANCED_FOR_STATEMENT) {
			return "ENHANCED_FOR_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.ENUM_CONSTANT_DECLARATION) {
			return "ENUM_CONSTANT_DECLARATION";
		}
		if(node.getNodeType()==ASTNode.ENUM_DECLARATION) {
			return "ENUM_DECLARATION";
		}
		if(node.getNodeType()==ASTNode.EXPORTS_DIRECTIVE) {
			return "EXPORTS_DIRECTIVE";
		}
		if(node.getNodeType()==ASTNode.EXPRESSION_METHOD_REFERENCE) {
			return "EXPRESSION_METHOD_REFERENCE";
		}
		if(node.getNodeType()==ASTNode.EXPRESSION_STATEMENT) {
			return "EXPRESSION_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.FIELD_ACCESS) {
			return "FIELD_ACCESS";
		}
		if(node.getNodeType()==ASTNode.FIELD_DECLARATION) {
			return "FIELD_DECLARATION";
		}
		if(node.getNodeType()==ASTNode.FOR_STATEMENT) {
			return "FOR_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.IF_STATEMENT) {
			return "IF_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.IMPORT_DECLARATION) {
			return "IMPORT_DECLARATION";
		}
		if(node.getNodeType()==ASTNode.INFIX_EXPRESSION) {
			return "INFIX_EXPRESSION";
		}
		if(node.getNodeType()==ASTNode.INITIALIZER) {
			return "INITIALIZER";
		}
		if(node.getNodeType()==ASTNode.INSTANCEOF_EXPRESSION) {
			return "INSTANCEOF_EXPRESSION";
		}
		if(node.getNodeType()==ASTNode.INTERSECTION_TYPE) {
			return "INTERSECTION_TYPE";
		}
		if(node.getNodeType()==ASTNode.LABELED_STATEMENT) {
			return "LABELED_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.LAMBDA_EXPRESSION) {
			return "LAMBDA_EXPRESSION";
		}
		if(node.getNodeType()==ASTNode.MARKER_ANNOTATION) {
			return "MARKER_ANNOTATION";
		}
		if(node.getNodeType()==ASTNode.MODIFIER) {
			return "MODIFIER";
		}
		if(node.getNodeType()==ASTNode.MODULE_MODIFIER) {
			return "MODULE_MODIFIER";
		}
		if(node.getNodeType()==ASTNode.MEMBER_VALUE_PAIR) {
			return "MEMBER_VALUE_PAIR";
		}
		if(node.getNodeType()==ASTNode.METHOD_DECLARATION) {
			return "METHOD_DECLARATION";
		}
		if(node.getNodeType()==ASTNode.METHOD_INVOCATION) {
			return "METHOD_INVOCATION";
		}
		if(node.getNodeType()==ASTNode.METHOD_REF) {
			return "METHOD_REF";
		}
		if(node.getNodeType()==ASTNode.METHOD_REF_PARAMETER) {
			return "METHOD_REF_PARAMETER";
		}
		if(node.getNodeType()==ASTNode.MODULE_DECLARATION) {
			return "MODULE_DECLARATION";
		}
		if(node.getNodeType()==ASTNode.NORMAL_ANNOTATION) {
			return "NORMAL_ANNOTATION";
		}
		if(node.getNodeType()==ASTNode.NAME_QUALIFIED_TYPE) {
			return "NAME_QUALIFIED_TYPE";
		}
		if(node.getNodeType()==ASTNode.NULL_LITERAL) {
			return "NULL_LITERAL";
		}
		if(node.getNodeType()==ASTNode.NUMBER_LITERAL) {
			return "NUMBER_LITERAL";
		}
		if(node.getNodeType()==ASTNode.PACKAGE_DECLARATION) {
			return "PACKAGE_DECLARATION";
		}
		if(node.getNodeType()==ASTNode.PARAMETERIZED_TYPE) {
			return "PARAMETERIZED_TYPE";
		}
		if(node.getNodeType()==ASTNode.PARENTHESIZED_EXPRESSION) {
			return "PARENTHESIZED_EXPRESSION";
		}
		if(node.getNodeType()==ASTNode.POSTFIX_EXPRESSION) {
			return "POSTFIX_EXPRESSION";
		}
		if(node.getNodeType()==ASTNode.PREFIX_EXPRESSION) {
			return "PREFIX_EXPRESSION";
		}
		if(node.getNodeType()==ASTNode.PRIMITIVE_TYPE) {
			return "PRIMITIVE_TYPE";
		}
		if(node.getNodeType()==ASTNode.QUALIFIED_NAME) {
			return "QUALIFIED_NAME";
		}
		if(node.getNodeType()==ASTNode.QUALIFIED_TYPE) {
			return "QUALIFIED_TYPE";
		}
		if(node.getNodeType()==ASTNode.RETURN_STATEMENT) {
			return "RETURN_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.SIMPLE_NAME) {
			return "SIMPLE_NAME";
		}
		if(node.getNodeType()==ASTNode.SIMPLE_TYPE) {
			return "SIMPLE_TYPE";
		}
		if(node.getNodeType()==ASTNode.SINGLE_VARIABLE_DECLARATION) {
			return "SINGLE_VARIABLE_DECLARATION";
		}
		if(node.getNodeType()==ASTNode.STRING_LITERAL) {
			return "STRING_LITERAL";
		}
		if(node.getNodeType()==ASTNode.SUPER_FIELD_ACCESS) {
			return "SUPER_FIELD_ACCESS";
		}
		if(node.getNodeType()==ASTNode.SUPER_METHOD_INVOCATION) {
			return "SUPER_METHOD_INVOCATION";
		}
		if(node.getNodeType()==ASTNode.SUPER_METHOD_REFERENCE) {
			return "SUPER_METHOD_REFERENCE";
		}
		if(node.getNodeType()==ASTNode.SUPER_CONSTRUCTOR_INVOCATION) {
			return "SUPER_CONSTRUCTOR_INVOCATION";
		}
		if(node.getNodeType()==ASTNode.SWITCH_CASE) {
			return "SWITCH_CASE";
		}
		if(node.getNodeType()==ASTNode.SWITCH_STATEMENT) {
			return "SWITCH_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.SYNCHRONIZED_STATEMENT) {
			return "SYNCHRONIZED_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.TAG_ELEMENT) {
			return "TAG_ELEMENT";
		}
		if(node.getNodeType()==ASTNode.TEXT_ELEMENT) {
			return "TEXT_ELEMENT";
		}
		if(node.getNodeType()==ASTNode.THIS_EXPRESSION) {
			return "THIS_EXPRESSION";
		}
		if(node.getNodeType()==ASTNode.THROW_STATEMENT) {
			return "THROW_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.TRY_STATEMENT) {
			return "TRY_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.TYPE_DECLARATION) {
			return "TYPE_DECLARATION";
		}
		if(node.getNodeType()==ASTNode.TYPE_DECLARATION_STATEMENT) {
			System.out.println("TYPE_DECLARATION_STATEMENT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+node.toString());
			return "TYPE_DECLARATION_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.TYPE_LITERAL) {
			return "TYPE_LITERAL";
		}
		if(node.getNodeType()==ASTNode.TYPE_METHOD_REFERENCE) {
			return "TYPE_METHOD_REFERENCE";
		}
		if(node.getNodeType()==ASTNode.TYPE_PARAMETER) {
			return "TYPE_PARAMETER";
		}
		if(node.getNodeType()==ASTNode.UNION_TYPE) {
			return "UNION_TYPE";
		}
		if(node.getNodeType()==ASTNode.USES_DIRECTIVE) {
			return "USES_DIRECTIVE";
		}
		if(node.getNodeType()==ASTNode.VARIABLE_DECLARATION_EXPRESSION) {
			return "VARIABLE_DECLARATION_EXPRESSION";
		}
		if(node.getNodeType()==ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return "VARIABLE_DECLARATION_FRAGMENT";
		}
		if(node.getNodeType()==ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			return "VARIABLE_DECLARATION_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.WHILE_STATEMENT) {
			return "WHILE_STATEMENT";
		}
		if(node.getNodeType()==ASTNode.WILDCARD_TYPE) {
			return "WILDCARD_TYPE";
		}
		return "";
	}
}
