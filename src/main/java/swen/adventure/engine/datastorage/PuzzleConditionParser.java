/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* David Barnett (barnetdavi) (300313764) */ 
 package swen.adventure.engine.datastorage;

import swen.adventure.engine.scenegraph.Puzzle;
import swen.adventure.engine.scenegraph.SceneNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 6/10/15.
 */
public class PuzzleConditionParser {

    /**
     * Parses a condition list in string format for a puzzle in the scene graph, and returns a list of PuzzleConditions.
     * Note that it is important that the nodes described have been declared before the puzzle in the XML file.
     * @param conditionListString The string to parse.
     * @param sceneGraph The scene graph in which to look for the target nodes.
     * @return A list of PuzzleConditions describing the conditions in the list.
     */
    @SuppressWarnings("unchecked")
    public static List<Puzzle.PuzzleCondition> parseConditionList(String conditionListString, SceneNode sceneGraph) {
        String[] conditions = conditionListString.replaceAll("\\s+", "").split(";");

        List<Puzzle.PuzzleCondition> conditionsList = new ArrayList<>();

        for (String conditionString : conditions) {
            String[] components = conditionString.split(",");

            Class typeClass = Boolean.class;

            int index = 0;
            if (components.length == 4) {
                try {
                    typeClass = Class.forName("java.lang." + components[index++]);
                } catch (ClassNotFoundException e) {
                    System.err.println("Error parsing condition list: " + e);
                }
            } else if (components.length != 3) {
                System.err.println("Error reading condition string " + conditionString + ": a condition must have three components.");
                break;
            }


            Optional<SceneNode> targetObject = sceneGraph.nodeWithID(components[index++]);
            if (!targetObject.isPresent()) {
                System.err.println("Error parsing condition list: could not find object with id " + components[index - 1]);
                break;
            }
            SceneNode sceneNode = targetObject.get();



            String getterName = components[index++];
            Supplier supplier;
            try {
                Method getter = sceneNode.getClass().getMethod(getterName);
                supplier = () -> {
                    try {
                        return getter.invoke(sceneNode);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException("Error retrieving property using getter " + getterName + ": " + e);
                    }
                };
            } catch (NoSuchMethodException e) {
                System.err.println("Error parsing condition list: " + e);
                break;
            }


            Object desiredValue = ParserManager.convertFromString(components[index], typeClass);

            conditionsList.add(new Puzzle.PuzzleCondition(supplier, desiredValue, conditionString));
        }

        return conditionsList;
    }
}