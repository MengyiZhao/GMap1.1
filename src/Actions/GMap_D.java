package Actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import SPNs.SPNProcessing;
import Tools.OAEIAlignmentSave;
import Tools.OAEIAlignmentOutput;
import Tools.OWLAPI_tools;
import Tools.OWLAPI_tools;
import Tools.Refine_Tools;
import Tools.Sim_Tools;
import Tools.TreeMap_Tools;
import Tools.HeuristicRule_Tools;

public class GMap_D {
	ArrayList<String> Alignments=new ArrayList<String>();	
	/*OWLAPI_tools onto1=new OWLAPI_tools();
	OWLAPI_tools onto2=new OWLAPI_tools();*/
	OWLAPI_tools onto1=new OWLAPI_tools();
	OWLAPI_tools onto2=new OWLAPI_tools();
	public static HashMap<String,String> entities_c1=new HashMap<String,String>();
	public static HashMap<String,String> entities_op1=new HashMap<String,String>();
	public static HashMap<String,String> entities_dp1=new HashMap<String,String>();
	public static HashMap<String,String> entities_c2=new HashMap<String,String>();
	public static HashMap<String,String> entities_op2=new HashMap<String,String>();
	public static HashMap<String,String> entities_dp2=new HashMap<String,String>();

	static Sim_Tools tool=new Sim_Tools();
	
	OAEIAlignmentOutput alignment_output;
	
	static BufferedWriter bfw_Result= null;
	static double a=0.7;
	static double contribution=0.2;
	static double threshold=0.8;	
	long tic = 0 ;
	long toc= 0;
	
	public void init(String p1,String p2)
	{
		onto1.readOnto(p1);
		onto2.readOnto(p2);
	}
	
	public void init(URL p1,URL p2) throws URISyntaxException
	{
		onto1.readOnto(p1);
		onto2.readOnto(p2);
	}
	
	public void println()
	{
		System.out.println("The alignments are listed as followed:");
		for(String a:Alignments)
			System.out.println(a);
	}
	
	public ArrayList<String> GetAligenmt()
	{
		String uri1=onto1.getURI();
		String uri2=onto2.getURI();
		ArrayList<String> matches=new ArrayList<String>();
		for(String a:Alignments)
		{
			String parts[]=a.split(",");
			String entity1=uri1+"#"+parts[0];
			String entity2=uri2+"#"+parts[1];
			String relation="=";
			String value=parts[2];
			matches.add(entity1+","+entity2+","+relation+","+value);
		}
		return matches;
	}
	
	public void SaveAligenmt() throws Exception
	{
		double time=System.currentTimeMillis();
		String alignmentPath="alignment/results/test"+(float)time;
		/*String alignmentPath="OM_SPN/Results_rdf/"+ontologyName1+"-"+ontologyName2;
		OAEIAlignmentOutput out=new OAEIAlignmentOutput(alignmentPath,ontologyName1,ontologyName2);*/
		OAEIAlignmentSave out=new OAEIAlignmentSave(alignmentPath,onto1.getURI(),onto2.getURI());
		for(int i=0;i<Alignments.size();i++)
		{
			String parts[]=Alignments.get(i).split(",");
			//System.out.println(Alignments.get(i));
			out.addMapping2Output(parts[0],parts[1],parts[2]);
		}
		out.saveOutputFile();
		System.out.println("The file is saved in "+alignmentPath);
	}
	
	public URL returnAlignmentFile() throws Exception 
	{
		align();
		//String alignmentPath="Results_rdf/"+ontologyName1+"-"+ontologyName2;
		alignment_output=new OAEIAlignmentOutput("alignment",onto1.getURI(),onto2.getURI());
		for(int i=0;i<Alignments.size();i++)
		{
			String parts[]=Alignments.get(i).split(",");
			//System.out.println(Alignments.get(i));
			alignment_output.addMapping2Output(parts[0],parts[1],parts[2]);
		}
		alignment_output.saveOutputFile();	
		return alignment_output.returnAlignmentFile();
	}
	

	public void align() throws ClassNotFoundException, IOException
	{	
		tool.initialPOS();
		String dictionary1="anatomy";
		String Read_path1="dic/"+dictionary1+".txt";
		
		/*File reader = new File(GMap3.class.getResource("/dic/anatomy.txt").getFile());
		String Read_path1=reader.getAbsolutePath();*/
		BufferedReader Result1 = new BufferedReader(new FileReader(new File(Read_path1)));
		ArrayList<String> dic1= new ArrayList<String>();
		String lineTxt = null;
		while ((lineTxt = Result1.readLine()) != null) {
			String line = lineTxt.trim(); // ȥ���ַ�����λ�Ŀո񣬱�����ո���ɵĴ���
			//line=line.toLowerCase();//ȫ�����Сд
			dic1.add(line);
		}
		HashMap<String, String> anatomy=transformToHashMap(dic1);
		
		ArrayList<String> classes1=onto1.getConcepts();
		ArrayList<String> classlabel1=onto1.getConceptAnnoations();
		ArrayList<String> objectProperties1=onto1.getObjectProperties();
		ArrayList<String> objectPropertieslabel1=onto1.getObjectPropertyAnnoations();
		ArrayList<String> dataProperties1=onto1.getDataProperties();
		ArrayList<String> dataPropertieslabel1=onto1.getDataPropertyAnnoations();
		ArrayList<String> propertiesInverse1=onto1.getPropertyAndInverse();//��ʵֻ��objectproperty���������	
		ArrayList<String> objectRelations1=onto1.getObjectRelations();
		//ArrayList<String> objectRelations1=onto1.GetObjectRelations();
		ArrayList<String> dataRelations1=onto1.getDataPropertyRelations();
		ArrayList<String> instances1=filterCommand(onto1.getConceptInstances());
		ArrayList<String> subclasses1=filterCommand(onto1.getSubClasses());
		ArrayList<String> superclasses1=filterCommand(onto1.getSuperClasses());
		ArrayList<String> subclassesDirect1=filterCommand(onto1.getDirectSubClasses());
		ArrayList<String> siblings1=filterCommand(onto1.getSibling(subclassesDirect1));
		ArrayList<String> disjoint1=filterCommand(onto1.getDisjointwith());

		ArrayList<String> EquivalentClass1=onto1.getEquivalentClass();
		ArrayList<String> EquivalentProperty1=onto1.getEquivalentObjectProperty();
		//ArrayList<String> Restrictions1=onto1.getSomeRestrictions();
		/*subclasses1=onto1.enhancedSubClasses(subclasses1,EquivalentClass1);
		superclasses1=onto1.enhancedSuperClasses(superclasses1,EquivalentClass1);
		disjoint1=onto1.enhancedClassesDisjoint(disjoint1,subclasses1,EquivalentClass1);*/
		objectRelations1=onto1.enhancedRelation(objectRelations1,EquivalentClass1);
		//objectRelations1=onto1.enhancedRelation(objectRelations1,Restrictions1);


		ArrayList<String> classes2=onto2.getConcepts();
		ArrayList<String> classlabel2=onto2.getConceptAnnoations();
		ArrayList<String> objectProperties2=onto2.getObjectProperties();
		ArrayList<String> objectPropertieslabel2=onto2.getObjectPropertyAnnoations();
		ArrayList<String> dataProperties2=onto2.getDataProperties();
		ArrayList<String> dataPropertieslabel2=onto2.getDataPropertyAnnoations();
		ArrayList<String> propertiesInverse2=onto2.getPropertyAndInverse();//��ʵֻ��objectproperty���������	
		ArrayList<String> objectRelations2=onto2.getObjectRelations();
		//ArrayList<String> objectRelations1=onto1.GetObjectRelations();
		ArrayList<String> dataRelations2=onto2.getDataPropertyRelations();
		ArrayList<String> instances2=filterCommand(onto2.getConceptInstances());
		ArrayList<String> subclasses2=filterCommand(onto2.getSubClasses());
		ArrayList<String> superclasses2=filterCommand(onto2.getSuperClasses());
		ArrayList<String> subclassesDirect2=filterCommand(onto2.getDirectSubClasses());
		ArrayList<String> siblings2=filterCommand(onto2.getSibling(subclassesDirect2));
		ArrayList<String> disjoint2=filterCommand(onto2.getDisjointwith());
		
		ArrayList<String> EquivalentClass2=onto2.getEquivalentClass();
		ArrayList<String> EquivalentProperty2=onto2.getEquivalentObjectProperty();
		//ArrayList<String> Restrictions2=onto2.getSomeRestrictions();
		/*subclasses2=onto2.enhancedSubClasses(subclasses2,EquivalentClass2);
		superclasses2=onto2.enhancedSuperClasses(superclasses2,EquivalentClass2);
		disjoint2=onto2.enhancedClassesDisjoint(disjoint2,subclasses2,EquivalentClass2);*/
		
		constructMap(classes1,"1c");
		constructMap(objectProperties1,"1op");
		constructMap(dataProperties1,"1dp");
		constructMap(classes2,"2c");
		constructMap(objectProperties2,"2op");
		constructMap(dataProperties2,"2dp");

		objectRelations2=onto2.enhancedRelation(objectRelations2,EquivalentClass2);
		//objectRelations2=onto2.enhancedRelation(objectRelations2,Restrictions2);

		/**
		 * calculate the similarities by ontology information
		 */
		/**
		 * Instances
		 */

		//������ֻ��һ�������Ҳ�Ϊ�յ���������ܵ������ƶ���һ���ģ�����Ľ��ռ䣩

		ArrayList<String> InstanceSim=tool.instancesSim2(classes1,classes2,instances1, instances2);

		
		/**
		 * concepts
		 */
		//��ֻ��һ����ţ���������������label

		/*toc = System.currentTimeMillis();
		System.out.println(toc-tic);*/
		ArrayList<String> editSimClass=new ArrayList<String>();
		ArrayList<String> newEditSimClass=tool.initialClass(classes1, classes2);
		// newEditSimClass=tool.ClassDisjoint(, );
		ArrayList<String> semanticSimClass=new ArrayList<String>();
		ArrayList<String> newSemanticSimClass=tool.initialClass(classes1, classes2);
		ArrayList<String> tfidfSim=new ArrayList<String>();
		TreeMap_Tools partOf1=new TreeMap_Tools();
		TreeMap_Tools partOf2=new TreeMap_Tools();
		TreeMap_Tools hasPart1=new TreeMap_Tools();
		TreeMap_Tools hasPart2=new TreeMap_Tools();
		HashMap<String,String> classLabels1=new HashMap<String,String>();
		HashMap<String,String> classLabels2=new HashMap<String,String>();

		boolean labelflag=classes1.size()==classlabel1.size()&&classes2.size()==classlabel2.size();//ֻ����ҽѧ�����вŻ����
		if(labelflag==true)
		{
			classLabels1=tool.transformToHashMap(classlabel1);//�洢һ�ݶ�Ӧ��ʽ
			classLabels2=tool.transformToHashMap(classlabel2);//�洢һ�ݶ�Ӧ��ʽ
			
			classlabel1=tool.keepLabel(classlabel1);
			classlabel2=tool.keepLabel(classlabel2);
			
			editSimClass=tool.editSimClass(classlabel1, classlabel2);

			//semanticSimClass=tool.semanticSimClass(classlabel1, classlabel2);//����Ҳ�ʵ������������ж���������
			semanticSimClass=tool.NewsemanticSimClass4(classlabel1, classlabel2);//����Ҳ�ʵ������������ж���������
			
			classlabel1=Normalize(classlabel1,anatomy);
			classlabel2=Normalize(classlabel2,anatomy);

			newEditSimClass=tool.editSimClass(classlabel1, classlabel2);
		
			newSemanticSimClass=tool.NewsemanticSimClass4(classlabel1, classlabel2);//����Ҳ�ʵ������������ж���������

			tfidfSim=tool.tfidfSim(classlabel1, classlabel2);


			
			ArrayList<String> localRestrictions1=onto1.getLocalSomeRestrictions();
			ArrayList<String> localRestrictions2=onto2.getLocalSomeRestrictions();
			partOf1=onto1.transformToPartOf(localRestrictions1);
			partOf2=onto2.transformToPartOf(localRestrictions2);
			hasPart1=onto1.transformToHaspart(localRestrictions1);
			hasPart2=onto2.transformToHaspart(localRestrictions2);

		}		
		else
		{	
			classlabel1=tool.briefLabel(classlabel1);
			classlabel2=tool.briefLabel(classlabel2);
			editSimClass=tool.editSimClassWithLabel(classes1, classes2,classlabel1,classlabel2);

			//��ʱ�俪�������ǵĻ�������2�ߵļ��㣬ֻ����label�滻������
			classlabel1=tool.replaceLabel(classes1,classlabel1);
			classlabel2=tool.replaceLabel(classes2,classlabel2);
			
			semanticSimClass=tool.semanticSimClass(classlabel1,classlabel2);//����Ҳ�ʵ������������ж���������

			tfidfSim=tool.tfidfSim(classlabel1,classlabel2);
		}

		/**
		 * ObjectProperties
		 */
		ArrayList<String> editSimObjectProperty=new ArrayList<String>();
		ArrayList<String> SemanticSimObjectProperty=new ArrayList<String>();
		boolean objectproperty_lableflag=false;
		objectproperty_lableflag=objectProperties1.size()==objectPropertieslabel1.size()&&objectProperties2.size()==objectPropertieslabel2.size();//ֻ����ҽѧ�����вŻ����
		ArrayList<String> objectPropertyPair=new ArrayList<String>();
		for(int i=0;i<objectProperties1.size();i++)
		{
			String objectproperty1=objectProperties1.get(i);
			for(int j=0;j<objectProperties2.size();j++)
			{
				String objectproperty2=objectProperties2.get(j);
				objectPropertyPair.add(objectproperty1+","+objectproperty2);
			}
		}
		if(objectproperty_lableflag==true)
		{
			//ֻ��label�����м���,һ���������ֻ������ҽѧ������
			objectPropertieslabel1=tool.keepLabel(objectPropertieslabel1);
			objectPropertieslabel2=tool.keepLabel(objectPropertieslabel2);

			editSimObjectProperty=tool.editSimProperty(objectPropertieslabel1, objectPropertieslabel2);

			SemanticSimObjectProperty=tool.semanticSimProperty(objectPropertieslabel1, objectPropertieslabel2);
		}
		else
		{
			//editSimClass=tool.editSimClassWithLabel(classes1, classes2,classlabel1,classlabel2);
			//��label�滻������༭����(�ֿ������̫��)
			objectPropertieslabel1=tool.briefLabel(objectPropertieslabel1);
			objectPropertieslabel2=tool.briefLabel(objectPropertieslabel2);
			
			editSimObjectProperty=tool.editSimPropertyWithLabel(objectProperties1, objectProperties2,objectPropertieslabel1,objectPropertieslabel2);

			objectPropertieslabel1=tool.replaceLabel(objectProperties1,objectPropertieslabel1);
			objectPropertieslabel2=tool.replaceLabel(objectProperties2,objectPropertieslabel2);

			SemanticSimObjectProperty=tool.semanticSimProperty(objectPropertieslabel1, objectPropertieslabel2);
		}

		/**
		 * datatypeProperties
		 */
		ArrayList<String> editSimDatatypeProperty=new ArrayList<String>();
		ArrayList<String> SemanticSimDatatypeProperty=new ArrayList<String>();	
		boolean dataproperty_lableflag=false;
		dataproperty_lableflag=dataProperties1.size()==dataPropertieslabel1.size()&&dataProperties2.size()==dataPropertieslabel2.size();//ֻ����ҽѧ�����вŻ����
		ArrayList<String> dataPropertyPair=new ArrayList<String>();
		for(int i=0;i<dataProperties1.size();i++)
		{
			String dataProperty1=dataProperties1.get(i);
			for(int j=0;j<dataProperties2.size();j++)
			{
				String dataProperty2=dataProperties2.get(j);
				dataPropertyPair.add(dataProperty1+","+dataProperty2);
			}
		}
		if(dataproperty_lableflag==true)
		{
			dataPropertieslabel1=tool.keepLabel(dataPropertieslabel1);
			dataPropertieslabel2=tool.keepLabel(dataPropertieslabel2);
			editSimDatatypeProperty=tool.editSimProperty(dataPropertieslabel1, dataPropertieslabel2);

			SemanticSimDatatypeProperty=tool.semanticSimProperty(dataPropertieslabel1, dataPropertieslabel2);
		}
		else
		{
			//��label�滻������༭����(�ֿ������̫��)
			dataPropertieslabel1=tool.briefLabel(dataPropertieslabel1);
			dataPropertieslabel2=tool.briefLabel(dataPropertieslabel2);
			
			editSimDatatypeProperty=tool.editSimPropertyWithLabel(dataProperties1,dataProperties2,dataPropertieslabel1, dataPropertieslabel2);

			dataPropertieslabel1=tool.replaceLabel(dataProperties1,dataPropertieslabel1);
			dataPropertieslabel2=tool.replaceLabel(dataProperties2,dataPropertieslabel2);
			SemanticSimDatatypeProperty=tool.semanticSimProperty(dataPropertieslabel1, dataPropertieslabel2);
		}

		//�Ժ�϶���ѭ��ģʽ(����Ӧ����ArrayList�ĸ�ʽ����д��)
		/**
		 * statistic the number of each pair that satisfys heuristic rules by ontology information
		 */

		ArrayList<String> classesMap=new ArrayList<String>();
		ArrayList<String> propertiesMap=new ArrayList<String>();
		ArrayList<String> objectPropertiesMap=new ArrayList<String>();		
		ArrayList<String> dataPropertiesMap=new ArrayList<String>();
		ArrayList<String> oldClassesMap=new ArrayList<String>();
		ArrayList<String> oldPropertiesMap=new ArrayList<String>();

		ArrayList<String> hiddenClassesMap=new ArrayList<String>();
		ArrayList<String> hiddenObjectPropertiesMap=new ArrayList<String>();
		ArrayList<String> hiddenDataPropertiesMap=new ArrayList<String>();

		Refine_Tools refineTools=new Refine_Tools();
		int iteration=0;
		boolean flag=false;
		boolean needComplementClass=true;
		boolean needComplementProperty=true;
		HashMap<String, Integer[]> Assignments=new HashMap<String, Integer[]>();
		SPNProcessing action=new SPNProcessing();

		//ArrayList<String> fathers=new ArrayList<String>();
		HashMap<String,Integer> fathers=new HashMap<String,Integer>();
		//ArrayList<String> children=new ArrayList<String>();
		HashMap<String,Integer> children=new HashMap<String,Integer>();
		ArrayList<String> siblings=new ArrayList<String>();
		//ArrayList<String> hasPart=new ArrayList<String>();
		HashMap<String,Integer> hasPart=new HashMap<String,Integer>();
		//ArrayList<String> partOf=new ArrayList<String>();
		HashMap<String,Integer> partOf=new HashMap<String,Integer>();
		/*ArrayList<String> domains=new ArrayList<String>();
		ArrayList<String> ranges=new ArrayList<String>();
		ArrayList<String> datatype=new ArrayList<String>();*/
		HashMap<String,Integer> domains=new HashMap<String,Integer>();
		HashMap<String,Integer> ranges=new HashMap<String,Integer>();
		HashMap<String,Integer> datatype=new HashMap<String,Integer>();
		
		/*ArrayList<String> disjointSim=new ArrayList<String>();
		disjointSim=tool.ClassDisjoint(classes1,classes2);//�����������,�����е�ƥ��Գ�ʼ��Ϊ'*'
*/		HashMap<String,String> disjointSim=new HashMap<String,String>();	
		ArrayList<String> classesAlignments=new ArrayList<String>();
		ArrayList<String> propertyAlignments=new ArrayList<String>();
		HeuristicRule_Tools ruleTools=new HeuristicRule_Tools(classesAlignments,propertyAlignments);

		do
		{
			//System.out.println("The iteration is "+ iteration);
			//����һ�ִ洢��ֵ
			oldClassesMap.clear();
			oldPropertiesMap.clear();

			classesAlignments=changeToAlignments(classesMap);
			propertyAlignments=changeToAlignments(propertiesMap);
			for(int i=0;i<classesAlignments.size();i++)
			{
				oldClassesMap.add(classesAlignments.get(i));
			}
			for(int i=0;i<propertyAlignments.size();i++)
			{
				oldPropertiesMap.add(propertyAlignments.get(i));
			}

			ruleTools.refreshAllMaps(classesAlignments,propertyAlignments);
			//HeuristicRule_Tools ruleTools=new HeuristicRule_Tools(classAlignmentPath,propertyAlignmentPath);
			/**
			 * find candidate maps among classes
			 */
			// bfw_Result.append("��ǰ��������Ϊ��"+iteration+" ����ƥ�����Ϊ��"+classesAlignments.size()+" ����ƥ�����Ϊ��"+propertyAlignments.size()+"\n");
			if(iteration>0)
			{
				fathers=ruleTools.fatherRule3(subclasses1,subclasses2);

				children=ruleTools.childrenRule3(superclasses1,superclasses2);

				siblings=ruleTools.siblingsRule2(siblings1,siblings2);
				
				hasPart=ruleTools.hasPartRule3(hasPart1,hasPart2);
			
				partOf=ruleTools.partOfRule3(partOf1,partOf2);

				HashMap<String,ArrayList<String>>  objectRelationMap1=changeRelationMap(objectRelations1);
				HashMap<String,ArrayList<String>>  objectRelationMap2=changeRelationMap(objectRelations2);

				ArrayList<String> lowerCaseClasses1=changeToLowerCase(classes1);
				ArrayList<String> lowerCaseClasses2=changeToLowerCase(classes2);
				domains=ruleTools.domainRule2(lowerCaseClasses1,lowerCaseClasses2,objectRelationMap1,objectRelationMap2);

				domains=ruleTools.rangeRule2(lowerCaseClasses1,lowerCaseClasses2,objectRelationMap1,objectRelationMap2);

				HashMap<String,ArrayList<String>>  dataRelationMap1=changeRelationMap(dataRelations1);
				HashMap<String,ArrayList<String>>  dataRelationMap2=changeRelationMap(dataRelations2);
				datatype=ruleTools.dataTypeRule2(lowerCaseClasses1,lowerCaseClasses2,dataRelationMap1,dataRelationMap2);

				//disjointSim=ruleTools.disjointRule(lowerCaseClasses1, lowerCaseClasses2, subclasses1, subclasses2, superclasses1, superclasses2, disjoint1, disjoint2);
				disjointSim=ruleTools.newDisjointRule(subclasses1, subclasses2, superclasses1, superclasses2, disjoint1, disjoint2);

			}


			//����Ҫ�����ԵĹ������,�����û�еģ���ΪClass�����Ե�Map��Ϊ��
			//TreeMap_Tools fatherRule=new TreeMap_Tools(fathers);
			//TreeMap_Tools childrenRule=new TreeMap_Tools(children);
			TreeMap_Tools siblingsRule=new TreeMap_Tools(siblings);
			//TreeMap_Tools partOfRule=new TreeMap_Tools(partOf);
			//TreeMap_Tools hasPartRule=new TreeMap_Tools(hasPart);
			//TreeMap_Tools domainsRule=new TreeMap_Tools(domains);
			//TreeMap_Tools rangesRule=new TreeMap_Tools(ranges);
			//TreeMap_Tools datatypeRule=new TreeMap_Tools(datatype);


			tic=System.currentTimeMillis();
			//SPNProcessing action=new SPNProcessing();
			//HashMap<String, Integer[]> Assignments=new HashMap<String, Integer[]>();//����Ϊȫ�ֵĻ���ʡ�ռ�
			ArrayList<String> roughMap=new ArrayList<String>();
			int classPairSize=InstanceSim.size();
			for(int i=0;i<classPairSize;i++)
			{
				/**
				 *  combine the lexical similarities of each pair
				 */
				double S0=0;
				//i=600;
				double editSimValue1=getTripleValue(editSimClass.get(i));
				double editSimValue2= getTripleValue(newEditSimClass.get(i));
				double editSimValue=Math.max(editSimValue1, editSimValue2);
				int editSize=getEditValue(editSimClass.get(i));
				double semanticSimValue1=getTripleValue(semanticSimClass.get(i));
				double semanticSimValue2=getTripleValue(newSemanticSimClass.get(i));
				double semanticSimValue=Math.max(semanticSimValue1, semanticSimValue2);
				double tfidfSimValue=getTripleValue(tfidfSim.get(i));
				String conceptPairs[]={};
				if(semanticSimValue==semanticSimValue1)
					conceptPairs=semanticSimClass.get(i).split(",");
				else
					conceptPairs=newSemanticSimClass.get(i).split(",");
				/*String concept1=conceptPairs[3];
				String concept2=conceptPairs[4];
				int length1=tool.tokeningWord(concept1).split(" ").length;
				int length2=tool.tokeningWord(concept2).split(" ").length;*/
				int length1=Integer.parseInt(conceptPairs[3]);
				int length2=Integer.parseInt(conceptPairs[4]);
				
				if(length1==1&&length2==1)
					S0=Math.max(editSimValue, semanticSimValue);
				else if(length1==length2&&length1!=1)//��ϴʣ�������ȵıȽ�
					S0=Math.max(editSimValue, Math.max(semanticSimValue, tfidfSimValue));
				else
					S0=Math.max(semanticSimValue, tfidfSimValue);
				
				if(editSimValue==1||(editSize==1&&semanticSimValue>=0.9))  //�����дʵ�����ͳһ���д���,���п��ܳ���1���ʵ����
					S0=1.0;
				
				/**	
				 * Use Instances similarity and Disjoint similarity to calculate the assignment of M in SPN.
				 */

				String pairInstanceValue[]=InstanceSim.get(i).split(",");
				String pairName=pairInstanceValue[0]+","+pairInstanceValue[1];
				String instance=pairInstanceValue[2];
				String disjoint=disjointSim.get(pairName.toLowerCase());
				if(disjoint==null)
					
					disjoint="*";
				Assignments.clear();
				Integer assignmentM[]={1,1,1};
				if(instance.equals("1")&&disjoint.equals("0"))
				{
					Integer assignmentD[]={0,1};
					Integer assignmentI[]={1,0,0};
					Assignments.put("D", assignmentD);
					Assignments.put("I", assignmentI);
				}
				else if(instance.equals("1")&&disjoint.equals("1"))
				{
					Integer assignmentD[]={1,0};
					Integer assignmentI[]={1,0,0};
					Assignments.put("D", assignmentD);
					Assignments.put("I", assignmentI);
				}
				else if(instance.equals("1")&&disjoint.equals("*"))
				{
					Integer assignmentD[]={1,1};
					Integer assignmentI[]={1,0,0};
					Assignments.put("D", assignmentD);
					Assignments.put("I", assignmentI);
				}
				else if(instance.equals("0")&&disjoint.equals("0"))
				{
					Integer assignmentD[]={0,1};
					Integer assignmentI[]={0,1,0};
					Assignments.put("D", assignmentD);
					Assignments.put("I", assignmentI);
				}
				else if(instance.equals("0")&&disjoint.equals("1"))
				{
					Integer assignmentD[]={1,0};
					Integer assignmentI[]={0,1,0};
					Assignments.put("D", assignmentD);
					Assignments.put("I", assignmentI);
				}
				else if(instance.equals("0")&&disjoint.equals("*"))
				{
					Integer assignmentD[]={1,1};
					Integer assignmentI[]={0,1,0};
					Assignments.put("D", assignmentD);
					Assignments.put("I", assignmentI);
				}
				else if(instance.equals("*")&&disjoint.equals("0"))
				{
					Integer assignmentD[]={0,1};
					Integer assignmentI[]={0,0,1};
					Assignments.put("D", assignmentD);
					Assignments.put("I", assignmentI);
				}
				else if(instance.equals("*")&&disjoint.equals("1"))
				{
					Integer assignmentD[]={1,0};
					Integer assignmentI[]={0,0,1};
					Assignments.put("D", assignmentD);
					Assignments.put("I", assignmentI);
				}
				else //ȫ��δ֪�����
				{
					Integer assignmentD[]={1,1};
					Integer assignmentI[]={0,0,1};
					Assignments.put("D", assignmentD);
					Assignments.put("I", assignmentI);
				}
				Assignments.put("M", assignmentM);
				//action=new SPNProcessing();//ִ����ô����ʼֵ��ԭ
				ArrayList<String> newAssignments=action.process(Assignments);
				String dAssign="",mAssign="";
				if(newAssignments.size()==1)//D���Ѿ�ָ�������
				{
					dAssign="D"+Integer.parseInt(disjoint);
					mAssign=newAssignments.get(0);		
				}
				else//D��unknown�����
				{
					dAssign=newAssignments.get(0);
					mAssign=newAssignments.get(1);
				}
			    double  A=a;
			     //a=0.7;
			    if(length1!=length2&&editSimValue!=1)
			    	A=A+0.1;

			    S0=Math.min(A*S0,a);
				    
				if(dAssign.equals("D1"))
					S0=0;
				else if(dAssign.equals("D0")&&mAssign.equals("M1"))
				{				
					S0=Math.min(S0+contribution, 1);
				}
				else if(dAssign.equals("D0")&&mAssign.equals("M2"))
					S0=Math.max(S0-contribution, 0);
					


				/**
				 * Use heuristic rules to improve the similarity in Noisy-OR model
				 */
				//String pairName=pairInstanceValue[0]+","+pairInstanceValue[1];
				double finalPositive=0;
				if(S0==0)   //������㲻�ཻ���ʣ��������ʵ������㣬��Ϊ���ƶ�Ϊ0
					finalPositive=0;
				else
				{			
					double N1=0;
					//if(satisfiedNum(fatherRule,pairName)>0)
					if(satisfiedNum(fathers,pairName)>0)	
						N1=1/(1+Math.exp(-satisfiedNum(fathers,pairName)));//������sigmoid������������������
					double N2=0;
					//if(satisfiedNum(childrenRule,pairName)>0)
					if(satisfiedNum(children,pairName)>0)
						N2=1/(1+Math.exp(-satisfiedNum(children,pairName)));
					double N3=0;//�ֵܽ�㣬������һ�ο��ŶȲ�����
					if(satisfiedNum(siblingsRule,pairName)>0)
						N3=1/(1+Math.exp(-satisfiedNum(siblingsRule,pairName)+1));
					double N4=0;
					//if(satisfiedNum(domainsRule,pairName)>0)
					if(satisfiedNum(domains,pairName)>0)
						N4=1/(1+Math.exp(-satisfiedNum(domains,pairName)));
					double N5=0;
					//if(satisfiedNum(rangesRule,pairName)>0)
					if(satisfiedNum(ranges,pairName)>0)
						N5=1/(1+Math.exp(-satisfiedNum(ranges,pairName)));
					double N6=0;
					//if(satisfiedNum(datatypeRule,pairName)>0)
					if(satisfiedNum(datatype,pairName)>0)
						N6=1/(1+Math.exp(-satisfiedNum(datatype,pairName)));
					double N7=0;
					//if(satisfiedNum(partOfRule,pairName)>0)
					if(satisfiedNum(partOf,pairName)>0)
						N7=1/(1+Math.exp(-satisfiedNum(partOf,pairName)));
					double N8=0;
					//if(satisfiedNum(hasPartRule,pairName)>0)
					if(satisfiedNum(hasPart,pairName)>0)
						N8=1/(1+Math.exp(-satisfiedNum(hasPart,pairName)));
				

					//�����Siָ���Ƕ�Ӧ�������ĸ��ʣ���ÿ��������Ӱ������ƥ�������Ͻ磩
					//ע�⸸���ӱ��ӶԸ�Ҫ��,1�ξͰ���0.7��ƥ��Գ�����ֵ
					//�ӶԸ��൱ 3�ξͰ���0.7��ƥ��Գ�����ֵ
					//�ֵܽ����ʱ�򲢲����ţ���һ�λ���ɺܶ౾���ϵ�����
					//��������ֵ��Ӱ���൱������������С �޷��Ͱ���0.7��ƥ��Գ�����ֵ
					//��С����Datatype���ܶ඼������String���Ͳ�ƥ��� �޷��Ͱ���0.7��ƥ��Գ�����ֵ
					double S1=0.45,S2=0.35,S3=0.35,S4=0.3,S5=0.3,S6=0.2,S7=0.30,S8=0.40;
					double finalNegative=(1-S0)*Math.pow(1-S1, N1)*Math.pow(1-S2, N2)*Math.pow(1-S3, N3)*
							Math.pow(1-S4, N4)*Math.pow(1-S5, N5)*Math.pow(1-S6, N6)*Math.pow(1-S7, N7)*Math.pow(1-S8, N8);
					finalPositive=1-finalNegative;
				}

				if(finalPositive>=threshold)
				{
					//System.out.println(pairName+","+finalPositive);
					roughMap.add(pairName+","+finalPositive+","+length1+","+length2);
				}
				else if(iteration==0&&finalPositive>=a*0.85&&finalPositive<=a)//�ܶ����صĵ㣬����֪ʶ���걸��ʵ��δ��������ϵδ���������������޷���ʶ��
				{
					hiddenClassesMap.add(pairName+","+finalPositive+","+length1+","+length2);
				}

			}
		 	//bfw_Result.append("��ǰ��������Ϊ��"+iteration+" SPN+Noisy_ORģ�ͼ������ĵ�ʱ��Ϊ��"+(toc-tic)/1000+"s"+"\n");
			//��ԭ���Ĳ���(false��Ե������ԣ�true��Ե���class)
			roughMap=addMaps(classesMap,roughMap); //classesMap�ӵ�roughMap��ȥ��һ����߱�ǰ�ߴ���Ҫ��һ�ָ���
			
			//Refine(ʮ�ֽ����1��1������)	
			ArrayList<String> refinedMap1=new ArrayList<String>();
			refinedMap1=refineTools.removeCrissCross(roughMap, superclasses1, superclasses2);	
		
			ArrayList<String> refinedMap2=new ArrayList<String>();
			refinedMap2=refineTools.keepOneToOneAlignment(refinedMap1);	
			//System.out.println("����1��1�ľ������ƥ��Եĸ���Ϊ:"+refinedMap2.size());
			//bfw_Result.append("��ǰ��������Ϊ��"+iteration+" ����One-One�����������ĵ�ʱ��Ϊ��"+(toc-tic)+"ms"+"\n");
			classesMap.clear();
			for(int i=0;i<refinedMap2.size();i++)
			{
				String part[]=refinedMap2.get(i).split(",");
				if(Double.parseDouble(part[2])>0)
				{
					/*String parts[]=refinedMap2.get(i).split(",");
					String label1=classLabels1.get(parts[0]);
					String label2=classLabels2.get(parts[1]);
					System.out.println(label1+","+label2+","+parts[2]);*/
					
					//System.out.println(refinedMap2.get(i));
					classesMap.add(refinedMap2.get(i));
				}			
			}

			/**
			 * find candidate maps among properties
			 */
			classesAlignments=changeToAlignments(classesMap);
			ruleTools.refreshClassMaps(classesAlignments);
			ArrayList<String> lowerCaseObjectProperties1=changeToLowerCase(objectProperties1);
			ArrayList<String> lowerCaseObjectProperties2=changeToLowerCase(objectProperties2);
			HashMap<String,ArrayList<String>>  objectRelationMap1=changeTripleMap(objectRelations1);
			HashMap<String,ArrayList<String>>  objectRelationMap2=changeTripleMap(objectRelations2);
			//ArrayList<String> objectProperties=ruleTools.objectPropertyRule(lowerCaseObjectProperties1, lowerCaseObjectProperties2, objectRelations1, objectRelations2);
			HashMap<String,Integer> objectProperties=ruleTools.objectPropertyRule2(lowerCaseObjectProperties1, lowerCaseObjectProperties2, objectRelationMap1, objectRelationMap2);

			HashMap<String,ArrayList<String>>  dataRelationMap1=changeTripleMap(dataRelations1);
			HashMap<String,ArrayList<String>>  dataRelationMap2=changeTripleMap(dataRelations2);
			ArrayList<String> lowerCaseDataProperties1=changeToLowerCase(dataProperties1);
			ArrayList<String> lowerCaseDataProperties2=changeToLowerCase(dataProperties2);
			//ArrayList<String> dataProperties=ruleTools.dataPropertyRule(lowerCaseDataProperties1, lowerCaseDataProperties2, dataRelations1, dataRelations2);
			HashMap<String,Integer>  dataProperties=ruleTools.dataPropertyRule2(lowerCaseDataProperties1, lowerCaseDataProperties2, dataRelationMap1, dataRelationMap2);

			//TreeMap_Tools OPrule=new TreeMap_Tools(objectProperties);
			//TreeMap_Tools DPrule=new TreeMap_Tools(dataProperties);

			ArrayList<String> roughObjectPropertyMap=new ArrayList<String>();


			int objectPropertyPairSize=editSimObjectProperty.size();
			for(int i=0;i<objectPropertyPairSize;i++)
			{
				double P0=0;
				double editSimValue=getTripleValue(editSimObjectProperty.get(i));
				double semanticSimValue=getTripleValue(SemanticSimObjectProperty.get(i));			
				P0=0.7*Math.max(editSimValue, semanticSimValue);//pooling�ķ���������һЩ��
				if(editSimValue==1)//ָ������ͬ���ַ���������
					P0=P0+0.0000001;

				/**
				 * Use heuristic rules to improve the similarity of properties in Noisy-OR model
				 */
				String part[]=editSimObjectProperty.get(i).split(",");
				String pairName=objectPropertyPair.get(i);						

				double M1=0;
			/*	if(satisfiedNum(OPrule,pairName)>0)
					M1=1/(1+Math.exp(-satisfiedNum(OPrule,pairName)));*/
				if(satisfiedNum(objectProperties,pairName)>0)
					M1=1/(1+Math.exp(-satisfiedNum(objectProperties,pairName))); //���ڶ�����ֵ���ƥ�䲢��sigmoid����������������						
				double S1=0.2;//����Ҫ2�β��ܳ�����ֵ0.75��		
				double finalNegative=(1-P0)*Math.pow(1-S1, M1);
				double finalPositive=1-finalNegative;
				if(finalPositive>=0.75)
				{
					//System.out.println(pairName+","+finalPositive);
					roughObjectPropertyMap.add(pairName+","+finalPositive+","+part[3]+","+part[4]+","+part[5]+","+part[6]);//Ӧ�ñ���5ά��Ϣ
				}	
				else if(finalPositive>0.7&&finalPositive<0.75&&iteration==0)//�ܶ����صĵ㣬����֪ʶ���걸��ʵ��δ��������ϵδ���������������޷���ʶ��
				{
					hiddenObjectPropertiesMap.add(pairName+","+finalPositive+","+part[3]+","+part[4]+","+part[5]+","+part[6]);
				}
			}
			//��ԭ���Ĳ���(false��Ե������ԣ�true��Ե���class)
			roughObjectPropertyMap=addMaps(objectPropertiesMap,roughObjectPropertyMap);//objectPropertiesMap�ӵ�roughObjectPropertyMap��ȥ��һ����߱�ǰ�ߴ���Ҫ��һ�ָ���

			//��������ƥ��Ե�һ��Refine�Ĺ���
			ArrayList<String> refinedObjectPropertyMap1=new ArrayList<String>();

			//�ø��º��classMap������propertyMap(�����Է������ȴ���к���)
		/*	refinedObjectPropertyMap1=refineTools.removeCrissCrossInProperty(roughObjectPropertyMap,classesAlignments,objectRelations1,objectRelations2);	
			//refinedPropertyMap1=refineTools.removeCrissCrossInProperty(roughPropertyMap,classAlignmentPath,relations1,relations2);	*/
			//System.out.println("���˻��ڶ�������ֵ����ƥ��ĸ���Ϊ:"+refinedObjectPropertyMap1.size());
		
			//propertiesInverse1; editSimProperty����ά�ṩ��Ϣ�����й���
			ArrayList<String> refinedObjectPropertyMap2=new ArrayList<String>();

			//stemObjectPropery=tool.findStemPair(refinedObjectPropertyMap1);
			//refinedObjectPropertyMap2=refineTools.removeStemConflict(refinedObjectPropertyMap1,propertiesInverse1,propertiesInverse2);	
			refinedObjectPropertyMap2=refineTools.removeStemConflict(roughObjectPropertyMap,propertiesInverse1,propertiesInverse2);	

			ArrayList<String> refinedObjectPropertyMap3=new ArrayList<String>();
			refinedObjectPropertyMap3=refineTools.keepOneToOneAlignment(refinedObjectPropertyMap2);	
			//System.out.println("����1��1�ľ������ƥ��Եĸ���Ϊ:"+refinedObjectPropertyMap3.size());
			propertiesMap.clear();
			objectPropertiesMap.clear();
			for(int i=0;i<refinedObjectPropertyMap3.size();i++)
			{
				String part[]=refinedObjectPropertyMap3.get(i).split(",");
				if(Double.parseDouble(part[2])>0)
				{
					objectPropertiesMap.add(refinedObjectPropertyMap3.get(i));
					propertiesMap.add(refinedObjectPropertyMap3.get(i));				
				}
			}

			ArrayList<String> roughDataPropertyMap=new ArrayList<String>();
			int dataPropertyPairSize=editSimDatatypeProperty.size();
			for(int i=0;i<dataPropertyPairSize;i++)
			{
				double P0=0;
				double editSimValue=getTripleValue(editSimDatatypeProperty.get(i));
				double semanticSimValue=getTripleValue(SemanticSimDatatypeProperty.get(i));			
				P0=0.7*Math.max(editSimValue, semanticSimValue);//pooling�ķ���������һЩ��
				if(editSimValue==1)//ָ������ͬ���ַ���������
					P0=P0+0.0000001;

				/**
				 * Use heuristic rules to improve the similarity of properties in Noisy-OR model
				 */
				String part[]=editSimDatatypeProperty.get(i).split(",");
				String pairName=dataPropertyPair.get(i);						

				double M2=0;
				/*if(satisfiedNum(DPrule,pairName)>0)
					M2=1/(1+Math.exp(-satisfiedNum(DPrule,pairName)));*/
				if(satisfiedNum(dataProperties,pairName)>0)
					M2=1/(1+Math.exp(-satisfiedNum(dataProperties,pairName)));//����DataType��ƥ�䣬����sigmoid����������������

				double S2=0.18;
				//����Ҫ3�β��ܳ�����ֵ0.75��	
				double finalNegative=(1-P0)*Math.pow(1-S2, M2);
				double finalPositive=1-finalNegative;
				if(finalPositive>=0.75)
				{
					//System.out.println(pairName+","+finalPositive);
					roughDataPropertyMap.add(pairName+","+finalPositive+","+part[3]+","+part[4]+","+part[5]+","+part[6]);//Ӧ�ñ���5ά��Ϣ
				}	
				else if(finalPositive>0.7&&finalPositive<0.75&&iteration==0)//�ܶ����صĵ㣬����֪ʶ���걸��ʵ��δ��������ϵδ���������������޷���ʶ��
				{
					hiddenDataPropertiesMap.add(pairName+","+finalPositive+","+part[3]+","+part[4]+","+part[5]+","+part[6]);
				}
			}
			//��ԭ���Ĳ���(false��Ե������ԣ�true��Ե���class)
			roughDataPropertyMap=addMaps(dataPropertiesMap,roughDataPropertyMap);//dataPropertiesMap�ӵ�roughDataPropertyMap��ȥ��һ����߱�ǰ�ߴ���Ҫ��һ�ָ���
		
			//������ֵ����ƥ��Ե�һ��Refine�Ĺ���		
			ArrayList<String> refinedPropertyMap=new ArrayList<String>();
			refinedPropertyMap=refineTools.keepOneToOneAlignment(roughDataPropertyMap);	
			//System.out.println("����1��1�ľ������ƥ��Եĸ���Ϊ:"+refinedPropertyMap.size());
			dataPropertiesMap.clear();
			for(int i=0;i<refinedPropertyMap.size();i++)
			{
				String part[]=refinedPropertyMap.get(i).split(",");
				if(Double.parseDouble(part[2])>0)
				{
					dataPropertiesMap.add(refinedPropertyMap.get(i));  //OWLAPI����������:���ܻ����ظ�������
					if(!propertiesMap.contains(refinedPropertyMap.get(i)))
						propertiesMap.add(refinedPropertyMap.get(i));
				}
			}

			//��������ƥ��ԣ������Ե�ƥ��Բ��ٷ����仯�����ߵ��������Ѿ���5�Σ�����ѭ����	
			propertyAlignments=changeToAlignments(propertiesMap);
			flag=unChange(classesAlignments,propertyAlignments,oldClassesMap,oldPropertiesMap);
			////���Ϊ�յĻ�����Ҫ��һЩ������ʮ�ֺ��ʵĵ���е��룬���൱���˹�ָ��
			if(classesMap.size()==0)
			{
				tic=System.currentTimeMillis();
				needComplementClass=true;		
				String type="class";
				hiddenClassesMap=Vibration(hiddenClassesMap,superclasses1,superclasses2);
				classesMap=complementMaps(classesMap,hiddenClassesMap,type);
				//classesMap=complementMaps(classesMap,hiddenClassesMap,type,needComplementClass);
				needComplementClass=false;
				toc=System.currentTimeMillis();
				//bfw_Result.append("��ǰ��������Ϊ��"+iteration+" ����ȱʡfirst-liner_class���ĵ�ʱ��Ϊ��"+(toc-tic)/1000+"s"+"\n");
			}
			if(propertiesMap.size()==0)
			{
				tic=System.currentTimeMillis();
				String type="property";
				//needComplementProperty=true;
				objectPropertiesMap=complementMaps(objectPropertiesMap,hiddenObjectPropertiesMap,type);
				objectPropertiesMap=refineTools.removeStemConflict(objectPropertiesMap,propertiesInverse1,propertiesInverse2);
				objectPropertiesMap=refineTools.keepOneToOneAlignment(objectPropertiesMap);	
				propertiesMap=addMaps(propertiesMap,objectPropertiesMap);//ǰ�����ϵģ��������µ�

				dataPropertiesMap=complementMaps(dataPropertiesMap,hiddenDataPropertiesMap,type);
				dataPropertiesMap=refineTools.keepOneToOneAlignment(dataPropertiesMap);
				propertiesMap=addMaps(propertiesMap,dataPropertiesMap);//ǰ�����ϵģ��������µ�
				needComplementProperty=false;
				toc=System.currentTimeMillis();
				//bfw_Result.append("��ǰ��������Ϊ��"+iteration+" ����ȱʡfirst-liner_property���ĵ�ʱ��Ϊ��"+(toc-tic)/1000+"s"+"\n");
			}
			iteration++;
			Boolean jump=!(iteration<4&&flag==false);
			if(jump==true)
			{
				if(needComplementClass==true)
				{
					classesMap=complementMaps(classesMap,hiddenClassesMap,"class");
					classesMap=refineTools.removeCrissCross(classesMap, superclasses1, superclasses2);
				}
				else//�����refineֻ�Ǽ�֦����������,��Ϊ�Ѿ���ӹ��ˣ����Խ�����1��1�����Ʋ���
					classesMap=newRefineClass(classesMap);	
					//classesMap=refineClass(classesMap,classes1,classes2);	
				//���һ��ʼû�в��䣬��Ϊ��ѭ���ھͻᲹ���,needComplementProperty��ֵ����Ϊfalse,��������֮ǰ�������һֱû��������
				if(needComplementProperty==true)
				{
					propertiesMap=complementMaps(propertiesMap,hiddenObjectPropertiesMap,"property");
					classesAlignments=changeToAlignments(classesMap);
					//propertiesMap=refineTools.removeCrissCrossInProperty(propertiesMap,classesAlignments,objectRelations1,objectRelations2);
					propertiesMap=refineTools.removeStemConflict(propertiesMap,propertiesInverse1,propertiesInverse2);
					propertiesMap=complementMaps(propertiesMap,hiddenDataPropertiesMap,"property");								
				}
				/*else   //��ʱ����Ҫ����
				propertiesMap=refineProperty(propertiesMap);*/
			}
		 //	bfw_Result.append("��ǰ��������Ϊ��"+(iteration-1)+" �������ĵ�ʱ��Ϊ��"+(iteration_toc-iteration_tic)/1000+"s"+"\n");
		}while(iteration<4&&flag==false);
		//���Ҳ��Ҫ�����ʵ��Ĳ���

		//���ǵ���Ϣ���걸��ʱ�򣬽���ЩǱ�ص�ƥ����в���(������������ʡ��ҲӦ���ܽ�Լ����ʱ��)
		/*if(needComplementClass==true)
			classesMap=complementMaps(classesMap,hiddenClassesMap,"class");
		else
			classesMap=refineClass(classesMap);
		//���һ��ʼû�в��䣬��Ϊ��ѭ���ھͻᲹ���,needComplementProperty��ֵ����Ϊfalse,��������֮ǰ�������һֱû��������
		if(needComplementProperty==true)
			propertiesMap=complementMaps(propertiesMap,hiddenObjectPropertiesMap,"property");
		else
			propertiesMap=refineProperty(propertiesMap);*/
		/*ArrayList<String> EquivalentClass2=onto2.GetEquivalentClass();
		ArrayList<String> EquivalentProperty2=onto2.GetEquivalentProperty();*/
		classesMap=enhancedMap(classesMap,EquivalentClass1);
		classesMap=enhancedMap(classesMap,EquivalentClass2);
		propertiesMap=enhancedMap(propertiesMap,EquivalentProperty1);
		propertiesMap=enhancedMap(propertiesMap,EquivalentProperty2);
	
		//System.out.println("���յĸ���ƥ���Ϊ��");
		for(int i=0;i<classesMap.size();i++)
		{
			String part[]=classesMap.get(i).split(",");	
			System.out.println(entities_c1.get(part[0].toLowerCase())+","+entities_c2.get(part[1].toLowerCase())+","+part[2]);
			Alignments.add(entities_c1.get(part[0].toLowerCase())+","+entities_c2.get(part[1].toLowerCase())+","+part[2]);
		}
		System.out.println("�ҵ�����ƥ��Եĸ���Ϊ��"+classesMap.size());

		System.out.println("���յ�����ƥ���Ϊ��");
		for(int i=0;i<propertiesMap.size();i++)
		{
			String part[]=propertiesMap.get(i).split(",");
			if((entities_op1.get(part[0].toLowerCase())!=null&&(entities_op2.get(part[1].toLowerCase())!=null)))
			{
				System.out.println(entities_op1.get(part[0].toLowerCase())+","+entities_op2.get(part[1].toLowerCase())+","+part[2]);
				Alignments.add(entities_op1.get(part[0].toLowerCase())+","+entities_op2.get(part[1].toLowerCase())+","+part[2]);
			}
			else
			{
				System.out.println(entities_dp1.get(part[0].toLowerCase())+","+entities_dp2.get(part[1].toLowerCase())+","+part[2]);
				Alignments.add(entities_dp1.get(part[0].toLowerCase())+","+entities_dp2.get(part[1].toLowerCase())+","+part[2]);
			}
		}
		System.out.println("�ҵ�����ƥ��Եĸ���Ϊ��"+propertiesMap.size());	
	}
	
	/**
	 * these function are just for preproccess
	 */
	public static ArrayList<String> filterCommand(ArrayList<String> maps)
	{
		ArrayList<String> fiteredMaps=new ArrayList<String>();
		for(int i=0;i<maps.size();i++)
		{
			String a=maps.get(i);
			fiteredMaps.add(a.replace("--,", "--"));
		}
		return fiteredMaps;
	}

	public static int satisfiedNum(HashMap<String,Integer> rule,String pair)
	{
		Integer value=rule.get(pair.toLowerCase());
		if(value!=null)
		{
			return value;
		}
		return 0;
	}
	
	public static int satisfiedNum(TreeMap_Tools rule,String pair)
	{
		ArrayList<String> value=rule.GetKey_Value(pair.toLowerCase());
		if(value!=null)
		{
			return (int) Double.parseDouble(value.get(0));
		}
		return 0;
	}

	public static double getTripleValue(String triple)
	{
		String part[]=triple.split(",");
		double value=Double.parseDouble(part[2]);
		return value;
	}
	
	public static boolean unChange(ArrayList<String> classesMap,ArrayList<String> propertiesMap,ArrayList<String> oldClassesMap, ArrayList<String> oldPropertiesMap)
	{
		boolean classUnChange=false;
		boolean propertyUnChange=false;
		if(oldClassesMap.size()!=classesMap.size())
		{
			return false;
		}
		if(oldPropertiesMap.size()!=propertiesMap.size())
		{
			return false;
		}			
		for(int i=0;i<classesMap.size();i++)
		{
			if(oldClassesMap.contains(classesMap.get(i)))
				classUnChange=true;
			else
			{
				classUnChange=false;
				break;
			}
		}
		if(propertiesMap.size()==0)
			propertyUnChange=true;
		for(int j=0;j<propertiesMap.size();j++)
		{
			if(oldPropertiesMap.contains(propertiesMap.get(j)))
				propertyUnChange=true;
			else
			{
				propertyUnChange=false;
				break;
			}
		}
		boolean change=classUnChange&&propertyUnChange;
		return change;
	}
	
	public static ArrayList<String> complementMaps(ArrayList<String> maps,ArrayList<String> hiddenmaps,String type)
	{
		Refine_Tools tools=new Refine_Tools();
		ArrayList<String> preciseMaps=new ArrayList<String>();	
		if(type.equals("class")&&maps.size()==0)//ClassΪ��ʱ�Ĳ���
		{
			for(int i=0;i<hiddenmaps.size();i++)				
			{
				maps.add(hiddenmaps.get(i));
			}
			preciseMaps=tools.keepOneToOneAlignment(maps);//���Class��ʱ������Ҫ�ġ����п��ܳ���1�Զ�����
			maps.clear();
			for(int i=0;i<preciseMaps.size();i++)
			{
				String parts[]=preciseMaps.get(i).split(",");
				if(Double.parseDouble(parts[2])>0)
					maps.add(preciseMaps.get(i));
			}
			return maps;	//ע�⣬���ﷵ�صĸ�ʽ����Ҫ�޸�
		}
		else if(type.equals("class")&&maps.size()!=0)//Class��Ϊ��ʱ�Ĳ���
		{
			preciseMaps=tools.keepOneToOneAlignment(hiddenmaps);
			for(int i=0;i<preciseMaps.size();i++)
			{
				boolean index=false;
				String parts1[]=preciseMaps.get(i).split(",");
				for(int j=0;j<maps.size();j++)
				{
					String parts2[]=maps.get(j).split(",");
					if(parts2[0].equals(parts1[0])||parts2[1].equals(parts1[1])||Double.parseDouble(parts1[2])==0)//1��1������
					{
						index=true;
						break;
					}
				}
				if(index==false)
				{
					//System.out.println(parts1[0]+"	"+parts1[1]);
					int length1=Integer.parseInt(parts1[3]);
					int length2=Integer.parseInt(parts1[4]);
					
					/*String concept1=parts1[0];
					String concept2=parts1[1];				
					int length1=tool.tokeningWord(concept1).split(" ").length;
					int length2=tool.tokeningWord(concept2).split(" ").length;*/
					if(length1==length2&&Double.parseDouble(parts1[2])>=a)//��������ͬ�Ÿ��迼�ǣ���Ϊ�кܶ�WordNet��������һ�����󣬵����ٻ��ʺܵ�
						maps.add(preciseMaps.get(i));	
					else if(length1!=length2&&Double.parseDouble(parts1[2])>=a*0.85)
						maps.add(preciseMaps.get(i));				
/*					if(length1==length2&&length1==1&&Double.parseDouble(parts1[2])>=a)//��������ͬ�Ÿ��迼�ǣ���Ϊ�кܶ�WordNet��������һ�����󣬵����ٻ��ʺܵ�
						maps.add(preciseMaps.get(i));
					if((length1==1&&length2>1)||(length1>1&&length2==1))	//����������ϴʵ������
						maps.add(preciseMaps.get(i));
					if(length1>1&&length2>1&&Double.parseDouble(parts1[2])>a*0.85)	//��ϴʵ�token������2����
						maps.add(preciseMaps.get(i));*/
				}
			}
			return maps;
		}
		else if((type.equals("property")&&maps.size()!=0))//���Բ�Ϊ�յ����
		{
			preciseMaps=tools.keepOneToOneAlignment(hiddenmaps);
			for(int i=0;i<preciseMaps.size();i++)
			{
				boolean index=false;
				String parts1[]=preciseMaps.get(i).split(",");
				for(int j=0;j<maps.size();j++)
				{
					String parts2[]=maps.get(j).split(",");
					if(parts2[0].equals(parts1[0])||parts2[1].equals(parts1[1])||Double.parseDouble(parts1[2])==0)//1��1������
					{
						index=true;
						break;
					}
				}
				if(index==false)//����ͻ��������
					maps.add(preciseMaps.get(i));
			}
			return maps;
		}
		else
			//else if(type.equals("property"))//��Ϊ����϶�Ϊ��,�����Ĺ��̱�����ѭ����Ԥ����һ�ξ���
		{
			for(int i=0;i<hiddenmaps.size();i++)				
			{
				maps.add(hiddenmaps.get(i));
			}
			return maps;	//ע�⣬���ﷵ�صĸ�ʽ����Ҫ�޸�
		}
	}
	
	public static ArrayList<String> addMaps(ArrayList<String> maps,ArrayList<String> newMaps)//ǰ�����ϵģ��������µ� classesMap,roughMap
	{
		//Sim_Tools tools=new Sim_Tools();
		ArrayList<String> preciseMaps=new ArrayList<String>();	

		for(int i=0;i<newMaps.size();i++)
		{
			preciseMaps.add(newMaps.get(i));
		}
		ArrayList<String> alignments=changeToAlignments(preciseMaps);
		for(int i=0;i<maps.size();i++)//����µ�MapsҪ����ֵ�Ĵ�С,�µı�ԭ����ֵҪ��
		{
			String parts[]=maps.get(i).split(",");
			String conceptPair=parts[0]+"--"+parts[1];
			if(alignments.contains(conceptPair))//��Ϊ�µ�ֵһ����ǰ�ߴ����Կ���pass��
			{
				continue;
			}
			else
				preciseMaps.add(maps.get(i));
		}
		return preciseMaps;	
	}
	
	public static ArrayList<String> newRefineClass(ArrayList<String> maps)//���㽫һЩ�����̫�ɿ�������û����SPN��Noisy-OR�еļ�ǿ��ƥ��Թ��˵�
	{
		//Refine_Tools tools=new Refine_Tools();
		ArrayList<String> preciseMaps=new ArrayList<String>();
		for(int i=0;i<maps.size();i++)
		{
			String parts[]=maps.get(i).split(",");	
			int length1=Integer.parseInt(parts[3]);
			int length2=Integer.parseInt(parts[4]);
			if(length1==length2&&length1==1&&Double.parseDouble(parts[2])>=a)//�����ַ�����Ϊ1���������ƶ�Ϊ0.9�� ������̫�����������
				preciseMaps.add(maps.get(i));
			else if(length1==length2&&length1!=1&&Double.parseDouble(parts[2])>a*0.9)//�����ַ�������ȣ�
				preciseMaps.add(maps.get(i));
			else if(length1!=length2&&Double.parseDouble(parts[2])>=a*0.85)
				preciseMaps.add(maps.get(i));
		}
		return preciseMaps;
	}
	
	public static HashMap<String,String> transformToHashMap(ArrayList<String> originalMap)
	{
		HashMap<String,String> standardMap=new HashMap<String,String>();
		for(int i=0;i<originalMap.size();i++)
		{
			String part[]=originalMap.get(i).split("--");
			standardMap.put(part[0],part[1]);
			//standardMap.add();
		}
		return standardMap;
	}
	
	public static ArrayList<String> changeToAlignments(ArrayList<String> maps)
	{
		ArrayList<String> alignments=new ArrayList<String>();
		for(int i=0;i<maps.size();i++)
		{
			String parts[]=maps.get(i).split(",");
			alignments.add(parts[0]+"--"+parts[1]);
		}
		return alignments;
	}

	public static ArrayList<String> enhancedMap(ArrayList<String> Map,ArrayList<String> Equivalent)
	{
		for(int i=0;i<Equivalent.size();i++)
		{
			String equivalent[]=Equivalent.get(i).split(",");
			if(equivalent.length==3&&equivalent[2].equals("Equal"))  //parts[0]��parts[1]�Ķ���
			{
				int index =findIndex(Map,equivalent[0]);
				if(index!=-1)
				{
					String newMap=Map.get(index).replaceFirst(equivalent[0], equivalent[1]);//�������
					String newMap2=Map.get(index).replaceFirst(equivalent[0].toLowerCase(), equivalent[1].toLowerCase());//�����
					if(!Map.contains(newMap))
						Map.add(newMap);
					else if(!Map.contains(newMap2))
					{
						Map.add(newMap2);
					}
				}
			}
		}
		return Map;
	}
	
	public static int findIndex(ArrayList<String> Map,String index)
	{
		//ArrayList<String> Triples=new ArrayList<String>();
		for(int i=0;i<Map.size();i++)
		{
			String parts[]=Map.get(i).split(",");
			if(index.toLowerCase().equals(parts[0].toLowerCase()))
			{
				return i;
			}
		}
		return -1;
	}
	
	public static HashMap<String,ArrayList<String>> changeRelationMap(ArrayList<String> triple)
	{
		HashMap<String,ArrayList<String>> relationMap=new HashMap<String,ArrayList<String>>();
		for(int i=0;i<triple.size();i++)
		{
			String part[]=triple.get(i).split(",");
			if(!relationMap.keySet().contains(part[1].toLowerCase())) //���key�����ڣ�����µ�(key,Value)��
			{
				ArrayList<String> a=new ArrayList<String>();
				a.add(part[0].toLowerCase()+"--"+part[2].toLowerCase());
				relationMap.put(part[1].toLowerCase(), a);
			}
			else	//���key���ڣ�ֱ�Ӷ�Value���Ͻ������
			{
				relationMap.get(part[1].toLowerCase()).add(part[0].toLowerCase()+"--"+part[2].toLowerCase());
			}
		}
		return relationMap;
	}
	
	public static HashMap<String,ArrayList<String>> changeTripleMap(ArrayList<String> triple)
	{
		HashMap<String,ArrayList<String>> relationMap=new HashMap<String,ArrayList<String>>();
		for(int i=0;i<triple.size();i++)
		{
			String part[]=triple.get(i).split(",");
			if(!relationMap.keySet().contains(part[0].toLowerCase())) //���key�����ڣ�����µ�(key,Value)��
			{
				ArrayList<String> a=new ArrayList<String>();
				a.add(part[1].toLowerCase()+"--"+part[2].toLowerCase());
				relationMap.put(part[0].toLowerCase(), a);
			}
			else	//���key���ڣ�ֱ�Ӷ�Value���Ͻ������
			{
				relationMap.get(part[0].toLowerCase()).add(part[1].toLowerCase()+"--"+part[2].toLowerCase());
			}
		}
		return relationMap;
	}

	public static ArrayList<String> Normalize(ArrayList<String> object,HashMap<String,String> dic)
	{
		ArrayList<String> normalizedThings=new ArrayList<String>();
		//int num=0;
		for(int i=0;i<object.size();i++)
		{
			//String part[]=object.get(i).split("--");
			String normalized=dic.get(object.get(i));
			if(normalized!=null)
			{
				String parts[]=normalized.split(" ");
				String pos= tool.findPOS(parts[0]);
				if(pos.equals("CD")||pos.equals("NNP"))//���ǵ�����ĸ��д������
				{
					String abbr_letter = parts[1].charAt(0)+parts[0];
					normalized=normalized.replace(parts[0], abbr_letter).replace(parts[1]+" ", "");
				}
				normalizedThings.add(normalized);
				//num++;
			}
			else
				normalizedThings.add(object.get(i));
			//standardMap.add();
		}
		//String candidate_num[]={"1","2","3","4","5","6","7","8","9"};
		//System.out.println("�淶������ĸ���Ϊ:"+num);
		return normalizedThings;
	}
	
	public static ArrayList<String> changeToLowerCase(ArrayList<String> things)
	{
		ArrayList<String> lowCaseArrayList=new ArrayList<String>();
		for(int i=0;i<things.size();i++)
		{
			lowCaseArrayList.add(things.get(i).toLowerCase());
		}
		return lowCaseArrayList;
	}
	
	public static int getEditValue(String triple)
	{
		String part[]=triple.split(",");
		int value=Integer.parseInt(part[3]);
		return value;
		
	}
	/*public static HashMap<String,String> entities_c1=new HashMap<String,String>();
	public static HashMap<String,String> entities_op1=new HashMap<String,String>();
	public static HashMap<String,String> entities_dp1=new HashMap<String,String>();
	public static HashMap<String,String> entities_c2=new HashMap<String,String>();
	public static HashMap<String,String> entities_op2=new HashMap<String,String>();
	public static HashMap<String,String> entities_dp2=new HashMap<String,String>();*/
	
	public static void constructMap(ArrayList<String> entities,String type)
	{
		if(type.equals("1c"))
		{			
			for(String e:entities)
			{
				//System.out.println(e);
				entities_c1.put(e.toLowerCase(),e);
			}
		}
		else if(type.equals("1op"))
		{			
			for(String e:entities)
			{
				//System.out.println(e);
				entities_op1.put(e.toLowerCase(),e);
			}
		}
		else if(type.equals("1dp"))
		{			
			for(String e:entities)
			{
				//System.out.println(e);
				entities_dp1.put(e.toLowerCase(),e);
			}
		}
		else if(type.equals("2c"))
		{			
			for(String e:entities)
			{
				//System.out.println(e);
				entities_c2.put(e.toLowerCase(),e);
			}
		}
		else if(type.equals("2op"))
		{			
			for(String e:entities)
			{
				//System.out.println(e);
				entities_op2.put(e.toLowerCase(),e);
			}
		}
		else if(type.equals("2dp"))
		{			
			for(String e:entities)
			{
				//System.out.println(e);
				entities_dp2.put(e.toLowerCase(),e);
			}
		}
		
	}
	
	public ArrayList<String> Vibration(ArrayList<String>maps,ArrayList<String> sup1,ArrayList<String> sup2)
	{
		ArrayList<String> candidateMap=new ArrayList<String>();
		ArrayList<String> needSelected=new ArrayList<String>();
		TreeMap_Tools Ontology1_sup=new TreeMap_Tools(sup1);
		TreeMap_Tools Ontology2_sup=new TreeMap_Tools(sup2);
		for(int i=0;i<maps.size();i++)
		{
			String parts[]=maps.get(i).split(",");		
			if(Double.parseDouble(parts[2])==0.6)
				needSelected.add(maps.get(i));
			else
				candidateMap.add(maps.get(i));
		}
		
		for(int k=0;k<needSelected.size();k++)
		{
			String part1s[]=needSelected.get(k).split(",");	
			String concept1=part1s[0];
			String concept2=part1s[1];
			for(int j=0;j<needSelected.size()-1;j++)
			{
				String part2s[]=needSelected.get(j+1).split(",");	
				String concept11=part2s[0];
				String concept22=part2s[1];
				if(concept1.equals(concept11)&&Ontology2_sup.has_relation(concept2, concept22)) //C��AC BC �������������A��B�Ķ���,��C,AC�Ƴ�
				{
					needSelected.remove(k);
					k--;
					break;
				}
				else if(concept2.equals(concept22)&&Ontology1_sup.has_relation(concept1, concept11))//AC BC��C�������������A��B�Ķ��ӣ���AC,C�Ƴ�
				{
					needSelected.remove(k);
					k--;
					break;
				}
				else if(concept1.equals(concept11)||concept2.equals(concept22)) //������ĸ��ӹ�ϵ������
				{
					continue;
				}
			}		
		}
		
		for(String a:needSelected)  //��ɸѡ��Ľ���������
			candidateMap.add(a);
		return candidateMap;
	}

}
