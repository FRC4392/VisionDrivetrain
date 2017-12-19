package org.usfirst.frc.team4932.vision;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VisionUpdate {
	protected boolean valid = false;
	protected long captured_ago_ms;
	protected List<TargetInfo> targets;
	protected double captured_at_timestamp = 0;
	private static JSONParser parser = new JSONParser();
	
	private static long getOptLong(Object n, long default_value) {
		if (n == null) {
			return default_value;
		}
		return (long) n;
	}
	
	private static Optional<Double> parseDouble(JSONObject j, String key) throws ClassCastException {
		Object d = j.get(key);
		if (d == null) {
			return Optional.empty();
		} else {
			return Optional.of((double) d);
		}
	}
	
	public static VisionUpdate generateFromJsonString(double current_time, String updateString) {
		VisionUpdate update = new VisionUpdate();
		try {
			JSONObject j = (JSONObject) parser.parse(updateString);
			long capturedAgoMs = getOptLong(j.get("CapturedAgoMs"), 0);
			if (capturedAgoMs == 0) {
				update.valid = false;
				return update;
			}
			
			update.captured_ago_ms = capturedAgoMs;
			update.captured_at_timestamp = current_time - capturedAgoMs / 1000.0;
			JSONArray targets = (JSONArray) j.get("targets");
			ArrayList<TargetInfo> targetInfos = new ArrayList<>(targets.size());
			for (Object targetObj : targets) {
				JSONObject target = (JSONObject) targetObj;
				Optional<Double> y = parseDouble(target, "y");
				Optional<Double> z = parseDouble(target, "z");
				if (!(y.isPresent() && z.isPresent())) {
					update.valid = false;
					return update;
				}
				
				targetInfos.add(new TargetInfo(y.get(), z.get()));
				
			}
			
			update.targets = targetInfos;
			update.valid = true;
		} catch (ParseException e) {
            System.err.println("Parse error: " + e);
            System.err.println(updateString);
        } catch (ClassCastException e) {
            System.err.println("Data type error: " + e);
            System.err.println(updateString);
        }
        return update;
	}
	
	public List<TargetInfo> getTargets(){
		return targets;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public long getCapturedAgoMs() {
		return captured_ago_ms;
	}
	
	public double getCapturedAtTimeStamp() {
		return captured_at_timestamp;
	}
}
