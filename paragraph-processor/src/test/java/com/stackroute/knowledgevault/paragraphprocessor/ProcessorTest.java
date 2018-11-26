package com.stackroute.knowledgevault.paragraphprocessor;

import com.stackroute.knowledgevault.paragraphprocessor.utilities.DocProcessor;
import com.stackroute.knowledgevault.paragraphprocessor.utilities.FillUpData;
import com.stackroute.knowledgevault.paragraphprocessor.utilities.Pair;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import static org.junit.Assert.*;

public class ProcessorTest {

    private Processor processor;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorTest.class);

    @Before
    public void setUp() {
        this.processor = new Processor();
    }

    @After
    public void tearDown() {
        this.processor = null;
    }

    @Test
    public void paraProcessingTest() {
        this.processor.setFilePath("../paragraph-processor/assets/taggerResource");
        this.processor.setIndexPath("../paragraph-processor/assets/taggerIndices");
        this.processor.getFullTextSearch().indexer(this.processor.getFilePath(),this.processor.getIndexPath());
        String paragraph = "my name is neeraj and i have been suffering from cancer in my lungs. I also have toothache for past 5days";
        LOGGER.info(paragraph);
        this.processor.paraProcessing(paragraph);
    }

    @Test
    public void keywordMapping() {
        this.processor.setFilePath("../paragraph-processor/assets/taggerResource/");
        this.processor.setIndexPath("../paragraph-processor/assets/taggerIndices");
        this.processor.getFullTextSearch().indexer(this.processor.getFilePath(),this.processor.getIndexPath());
        String tag = this.processor.keywordMapping("cancer");
        assertEquals("diseases",tag);
    }

    @Test
    public void testPatternMatch() {
        String dur = "5days";
        assertEquals("not found",this.processor.patMatch(dur));
    }

    @Test
    public void jsonldGenTest() {
        Map<String,List<Pair>> tags = new HashMap<>();
        String para = "The disease burden is high in India, for obvious reasons like poor sanitation, lack of access to fresh water, poor hygiene, etc., which are common in the most developing countries. Though exact dependable statistics are not available, a good percentage of cases go unreported. Secondly, ‘infection is not recognized till it becomes symptomatic.\\n\\nCholera is an acute diarrhoeal disease caused by V. Cholera (classical or El T). It is now commonly due to the El T or biotype. The majority of infections are mild or symptomatic. Epidemics of cholera are characteristically abrupt and often create an acute public health problem. They have a high potential to spread fast and cause deaths. The epidemic reaches a peak and subsides gradually as the ‘force of infection declines. Often, when time control measures are instituted, the epidemic has already reached its peak and is waning\\n\\nTyphoid fever is an acute, systemic infection presenting as fever with abdominal symp\\u00adtoms, caused by Salmonella typhi and paratyphi. Before nineteenth century, typhus and typhoid fever were considered to be the same. Enteric fever is an alternative name for typhoid. Salmonella typhi and paratyphi colonise only humans.\\n\\nThe organisms are acquired via ingestion of food or water, contaminated with human excreta from infected persons. Direct person-to-person transmission is rare. Typhoid is a global health problem. It is seen in children older than the age of one.\\n\\nOutbreak of typhoid in developing countries results in high mortality. The recent development of antibiotic resistant organisms is causing much concern. Typhoid fever is more common in the tropics. It tends to occur in places, where the sanitation standards are poor. A bacterial organism called salmonella typhi causes typhoid fever.";
        Map<String,Double> keys = DocProcessor.performNGramAnalysis(para);
        LOGGER.info("returned keys: {}",keys.keySet());

        for(Map.Entry<String,Double> key: keys.entrySet()) {

            File dictionary = new File("../../knowledge-vault/paragraph-processor/assets/taggerResource/");
            for(File f: dictionary.listFiles()) {

                tags.putIfAbsent(f.getName(),new ArrayList<>());
                try(BufferedReader br = new BufferedReader(new FileReader(f))) {
                    String txt;
                    while((txt = br.readLine())!=null) {
                        if(txt.length()>0 && DocProcessor.validKey(txt,key.getKey())) {
                            Pair p = new Pair(txt.trim(),key.getValue());
                            tags.get(f.getName()).add(p);
                            break;
                        }
                    }
                } catch( Exception e) {
                    e.printStackTrace();
                }
            }
        }
        LOGGER.info(tags.toString());

        JSONObject obj = FillUpData.fillOntology(tags);
    }
}