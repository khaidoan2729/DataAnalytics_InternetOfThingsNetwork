package cpen221.mp3.server;

import java.util.*;
import cpen221.mp3.event.*;
import cpen221.mp3.client.*;


public class Predictor {

    private List<Double> requiredDataDouble;        // For predicting N number of values
    private List<Boolean> requiredDataBoolean;      // For predicting N number of values
    private List<Double> requiredTimestamps;    // For predicting N number of timestamps
    public boolean isActuatorEvents;
    private int numberOfPredictions;

    /**
     * Constructor for predicting N number of values
     * @param sortedData list of sorted Events based on timestamps
     * @param numberOfPredictions number of predictions that need to be made
     * **/

    public Predictor(List<Event> sortedData, int numberOfPredictions) {
        this.numberOfPredictions = numberOfPredictions;
        // Checks what type of Event this is
        if (sortedData.get(0).getClass().toString().contains("ActuatorEvent")) {
            this.isActuatorEvents = true;
            this.requiredDataBoolean = new ArrayList<>();
        }
        else {
            this.requiredDataDouble = new ArrayList<>();
            this.isActuatorEvents = false;
        }

        sortedData.sort((e2, e1) -> Double.compare(e2.getTimeStamp(), e1.getTimeStamp()));

        for (int i = 0; i < sortedData.size() ; i++) {
            if (this.isActuatorEvents)
                this.requiredDataBoolean.add(sortedData.get(i).getValueBoolean());
            else
                this.requiredDataDouble.add(sortedData.get(i).getValueDouble());
        }
    }

    public Predictor (List<Double> timestampList, int numberOfPredictions, boolean isPredictingTimeStamp){
        this.requiredTimestamps = new ArrayList<>();
        this.requiredTimestamps.addAll(timestampList);
        this.numberOfPredictions = numberOfPredictions;
        this.isActuatorEvents = false;
    }


    public List<Double> makeNValuePredictionsDouble(){
        List<Double> oddSideList = new ArrayList<>();
        List<Double> evenSideList = new ArrayList<>();
        List<Double> predictionList = new ArrayList<>();
        if (this.requiredDataDouble.isEmpty())
            return predictionList;

        for (int i = 0; i < this.requiredDataDouble.size(); i++) {
            if (i % 2 == 0){
                evenSideList.add(this.requiredDataDouble.get(i));
            } else {
                oddSideList.add(this.requiredDataDouble.get(i));
            }
        }
        if (isIdenticalListDouble(evenSideList) && isIdenticalListDouble(oddSideList)){
            if (this.requiredDataDouble.size() % 2 == 0) {
                for (int i = 0; i < this.numberOfPredictions; i++) {
                    if (i % 2 == 0) predictionList.add(evenSideList.get(0));
                    else predictionList.add(oddSideList.get(0));
                }
            } else {
                for (int i = 0; i < this.numberOfPredictions; i++) {
                    if (i % 2 == 0) predictionList.add(oddSideList.get(0));
                    else predictionList.add(evenSideList.get(0));
                }
            }
        } else {
            for (int i = 0; i < this.numberOfPredictions; i++) {
                int count = 0;
                int sum = 0;
                for (Double num : this.requiredDataDouble){
                    count ++;
                    sum += num;
                }
                for (Double num : predictionList){
                    count ++;
                    sum += num;
                }
                double nextVal = sum / count;
                predictionList.add(nextVal);
            }
        }

        return predictionList;
    }



    public List<Boolean> makeNValuePredictionsBoolean(){
        List<Boolean> oddSideList = new ArrayList<>();
        List<Boolean> evenSideList = new ArrayList<>();
        List<Boolean> predictionList = new ArrayList<>();
        if (this.requiredDataBoolean.isEmpty())
            return predictionList;

        for (int i = 0; i < this.requiredDataBoolean.size(); i++) {
            if (i % 2 == 0){
                evenSideList.add(this.requiredDataBoolean.get(i));
            } else {
                oddSideList.add(this.requiredDataBoolean.get(i));
            }
        }
        if (isIdenticalListBoolean(evenSideList) && isIdenticalListBoolean(oddSideList)){
            if (this.requiredDataBoolean.size() % 2 == 0) {
                for (int i = 0; i < this.numberOfPredictions; i++) {
                    if (i % 2 == 0) predictionList.add(evenSideList.get(0));
                    else predictionList.add(oddSideList.get(0));
                }
            } else {
                for (int i = 0; i < this.numberOfPredictions; i++) {
                    if (i % 2 == 0) predictionList.add(oddSideList.get(0));
                    else predictionList.add(evenSideList.get(0));
                }
            }
        } else {
            List<Boolean> booleanValuePool = new ArrayList<>();
            for (Boolean num : this.requiredDataBoolean){
                booleanValuePool.add(num);
            }

            for (int i = 0; i < this.numberOfPredictions; i++) {
                boolean nextVal = true;
                int numberOfTrue = 0;
                int numberOfFalse = 0;

                for (Boolean bool : booleanValuePool) {
                    if (bool) numberOfTrue ++;
                    else numberOfFalse ++;
                }

                double ratioOfTrue = numberOfTrue / booleanValuePool.size();
                double ratioOfFalse = numberOfFalse / booleanValuePool.size();

                Random random = new Random();
                double randomRatio = random.nextDouble();


                if (ratioOfFalse < ratioOfTrue) {           // If ratio of false is less than ration of true values
                    if (randomRatio > ratioOfFalse)             // If random generated ratio is more than ratio of false values
                        predictionList.add(true);               // more prone to be true
                    else
                        predictionList.add(false);              // more prone to be false
                } else {                                    // If ratio of false is more than or equal to ratio of true values
                    if (randomRatio < ratioOfTrue)              // more prone to be true
                        predictionList.add(true);
                    else
                        predictionList.add(false);              // more prone to be false
                }

            }
        }
        if (predictionList.size() != this.numberOfPredictions)
            try {
                throw new Exception();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        return predictionList;
    }


    public List<Double> makeNValuePredictionsTimestamp() {
        List <Double> predictionList = new ArrayList<>();
        List <Double> differenceList = new ArrayList<>();

        if (this.requiredTimestamps.isEmpty())
            return predictionList;

        List <Double> history = new ArrayList<>();
        history.addAll(this.requiredTimestamps);

        for (int j = 0; j < history.size()-1; j++) {
            differenceList.add(Math.abs(history.get(j+1) - history.get(j)));
        }

        for (int i = 0; i < this.numberOfPredictions; i++) {
            Double sum = (double) 0;
            for (Double num : differenceList)
                sum += num;
            Double nextTimestamp = history.get(0) + ((sum * 1.0)/differenceList.size()) ;
            differenceList.add(0, nextTimestamp - history.get(0));
            history.add(0, nextTimestamp);
            predictionList.add(0, nextTimestamp);
        }


        return predictionList;
    }


        public boolean isIdenticalListDouble(List<Double> list) {
        for (int i = 0 ; i < list.size() -1; i++) {
            if (!(list.get(i) == (list.get(i+1))))
                return false;
        }
        return true;
    }

    public boolean isIdenticalListBoolean(List<Boolean> list) {
        for (int i = 0 ; i < list.size() -1; i++) {
            if (!list.get(i).equals(list.get(i+1)))
                return false;
        }
        return true;
    }

    public static void main(String[] args) {
        List <Event> ts = new ArrayList<>();
        Event e1 = new SensorEvent(1,1,1,"TempSensor", 21);
        Event e2 = new SensorEvent(2,1,1,"TempSensor", 22);
        Event e3 = new SensorEvent(3,1,1,"TempSensor", 21);
        Event e4 = new SensorEvent(4,1,1,"TempSensor", 22);
        Event e5 = new SensorEvent(5,1,1,"TempSensor", 21);
        ts.add(e1);
        ts.add(e2);
        ts.add(e3);
        ts.add(e4);
        ts.add(e5);

        Predictor p = new Predictor(ts, 5);
        System.out.println(p.makeNValuePredictionsDouble());
    }

}
