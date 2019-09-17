package cecs429.text;

import org.tartarus.snowball.SnowballStemmer;

public class PorterStemmer {
	private SnowballStemmer m_Stemmer = null;
	
	public PorterStemmer()
	{
	    Class stemClass = null;
		try {
			stemClass = Class.forName("org.tartarus.snowball.ext." + "english" + "Stemmer");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			m_Stemmer = (SnowballStemmer) stemClass.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String GetStemmedToken(String s)
	{
		m_Stemmer.setCurrent(s);
		m_Stemmer.stem();
		return m_Stemmer.getCurrent();
	}
}
