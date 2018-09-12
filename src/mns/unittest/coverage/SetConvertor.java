package mns.unittest.coverage;

import java.util.Iterator;
import java.util.Set;

public class SetConvertor {

	public String setConvertor(Set<String> values) {
		StringBuilder sb = new StringBuilder();
		Iterator<String> iter = values.iterator();
		while (iter.hasNext()) {
			sb.append(iter.next());
			sb.append("</br>");
		}

		return sb.toString();
	}

}