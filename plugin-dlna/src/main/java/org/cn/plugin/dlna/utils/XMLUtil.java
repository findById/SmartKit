package org.cn.plugin.dlna.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenning on 17-1-13.
 */

public class XMLUtil {

    public static Map<String, String> parseSetupOne(String xml) {
        Map<String, String> map = new HashMap<>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            StringReader sr = new StringReader(xml);
            InputSource is = new InputSource(sr);
            Document document = db.parse(is);
            Element element = document.getDocumentElement();

            NodeList modelName = element.getElementsByTagName("modelName");
            System.out.println(modelName.item(0).getTextContent());

            map.put("friendlyName", element.getElementsByTagName("friendlyName").item(0).getFirstChild().getNodeValue());
            map.put("modelName", element.getElementsByTagName("modelName").item(0).getFirstChild().getNodeValue());
            map.put("UDN", element.getElementsByTagName("UDN").item(0).getFirstChild().getNodeValue());

            map.putAll(parseServiceList(element.getElementsByTagName("serviceList")));
            map.putAll(parseIcon(element.getElementsByTagName("iconList")));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return map;
    }

    private static Map<String, String> parseServiceList(NodeList nodeList) {
        Map<String, String> map = new HashMap<>();
        try {
            for (int i = 0; i < nodeList.getLength(); i++) {
                NodeList childs = nodeList.item(i).getChildNodes();
                for (int j = 0; j < childs.getLength(); j++) {
                    NodeList child = childs.item(j).getChildNodes();
                    boolean av = false;
                    for (int k = 0; k < child.getLength(); k++) {
                        Node c = child.item(k);
                        if (av && c.getFirstChild() != null) {
                            map.put(c.getNodeName(), c.getFirstChild().getNodeValue());
                            continue;
                        }
                        if ("serviceType".equalsIgnoreCase(c.getNodeName())
                                && "urn:schemas-upnp-org:service:AVTransport:1".equalsIgnoreCase(c.getFirstChild().getNodeValue())) {
                            av = true;
                            map.put(c.getNodeName(), c.getFirstChild().getNodeValue());
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return map;
    }

    private static Map<String, String> parseIcon(NodeList iconList) {
        Map<String, String> map = new HashMap<>();
        try {
            if (iconList == null || iconList.getLength() <= 0) {
                return map;
            }
            for (int i = 0; i < iconList.getLength(); i++) {
                NodeList childs = iconList.item(i).getChildNodes();
                for (int j = 0; j < childs.getLength(); j++) {
                    NodeList child = childs.item(j).getChildNodes();
                    boolean av = false;
                    String mimetype = null;
                    String width = null;
                    String url = null;
                    for (int k = 0; k < child.getLength(); k++) {
                        Node c = child.item(k);
                        if (c == null || c.getFirstChild() == null) {
                            continue;
                        }
                        if ("mimetype".equalsIgnoreCase(c.getNodeName())) {
                            mimetype = c.getFirstChild().getNodeValue().replace("/", "");
                        }
                        if ("width".equalsIgnoreCase(c.getNodeName())) {
                            width = c.getFirstChild().getNodeValue();
                        }
                        if ("url".equalsIgnoreCase(c.getNodeName())) {
                            url = c.getFirstChild().getNodeValue();
                        }
                        if (mimetype != null && width != null && url != null) {
                            map.put(mimetype + width, url);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return map;
    }
}
