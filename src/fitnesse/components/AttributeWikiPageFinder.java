package fitnesse.components;

import static fitnesse.responders.editing.PropertiesResponder.SUITES;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class AttributeWikiPageFinder extends WikiPageFinder {

  private static List<String> setUpPageNames = Arrays.asList("SetUp", "SuiteSetUp");
  private static List<String> tearDownPageNames = Arrays.asList("TearDown", "SuiteTearDown");

  private List<String> requestedPageTypes;
  private Map<String, Boolean> attributes;
  private List<String> suites;

  public AttributeWikiPageFinder(SearchObserver observer, List<String> requestedPageTypes, Map<String, Boolean> attributes, String suites) {
    super(observer);
    this.requestedPageTypes = requestedPageTypes;
    this.attributes = attributes;

    if (suites != null)
      this.suites = Arrays.asList(splitSuitesIntoArray(suites));
  }

  protected boolean pageMatches(WikiPage page) throws Exception {
    if (attributes.containsKey("SetUp") && attributes.get("SetUp") != isSetUpPage(page)) {
      return false;
    }

    if (attributes.containsKey("TearDown") && attributes.get("TearDown") != isTearDownPage(page)) {
      return false;
    }

    PageData pageData = page.getData();

    if (!pageIsOfRequestedPageType(page, requestedPageTypes)) {
      return false;
    }

    for (Map.Entry<String, Boolean> input : attributes.entrySet()) {
      if ("SetUp".equals(input.getKey()) || "TearDown".equals(input.getKey()))
        continue;

      if (!attributeMatchesInput(pageData.hasAttribute(input.getKey()), input
          .getValue()))
        return false;
    }

    return suitesMatchInput(pageData, suites);
  }

  private boolean isTearDownPage(WikiPage page) throws Exception {
    return tearDownPageNames.contains(page.getName());
  }

  private boolean isSetUpPage(WikiPage page) throws Exception {
    return setUpPageNames.contains(page.getName());
  }

  private boolean pageIsOfRequestedPageType(WikiPage page,
      List<String> requestedPageTypes) throws Exception {
    PageData data = page.getData();
    if (data.hasAttribute("Suite")) {
      return requestedPageTypes.contains("Suite");
    }

    if (data.hasAttribute("Test")) {
      return requestedPageTypes.contains("Test");
    }

    return requestedPageTypes.contains("Normal");
  }

  protected boolean attributeMatchesInput(boolean attributeSet,
      boolean inputValueOn) {
    return attributeSet == inputValueOn;
  }

  private boolean suitesMatchInput(PageData pageData, List<String> suites)
  throws Exception {
    if (suites == null)
      return true;

    String suitesAttribute = pageData.getAttribute(SUITES);
    List<String> suitesProperty = Arrays.asList(splitSuitesIntoArray(suitesAttribute));

    if (suites.isEmpty() && !suitesProperty.isEmpty())
      return false;

    for (String suite : suites) {
      if (!suitesProperty.contains(suite))
        return false;
    }
    return true;
  }

  private String[] splitSuitesIntoArray(String suitesInput) {
    if (suitesInput == null || isEmpty(suitesInput))
      return new String[0];

    return suitesInput.split("\\s*,\\s*");
  }

  private boolean isEmpty(String checkedString) {
    for (char character: checkedString.toCharArray()) {
      if (!Character.isWhitespace(character))
        return false;
    }
    return true;
  }

}
