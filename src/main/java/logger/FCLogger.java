package logger;
import java.time.LocalDateTime; // import the LocalTime class
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.format.DateTimeFormatter; // Import the DateTimeFormatter class
import java.util.Date;

import javax.lang.model.type.ErrorType;

import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.InvocationEvent;
import com.salesforce.functions.jvm.sdk.SalesforceFunction;
import com.google.gson.Gson;
import java.lang.Math;
import java.lang.reflect.InvocationTargetException;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class FCLogger {
    private static final Logger logger = LoggerFactory.getLogger(FCLogger.class);
    private Object event;
    private Context context;
    // private Logger logger;
    public Date startedAt;
    private long memoryBefore;
    private LDObject outStream = new LDObject();

    public FCLogger(InvocationEvent<Object> event, Context sfcontext) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        // this.logger = logger;
        this.event = event;
        this.context = sfcontext;

        this.memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Map<String, Object> eventMap = objectMapper.convertValue(event, Map.class);

        outStream.fcLoggerVersion = "1.0";
        outStream.invocationId = BeanUtils.getNestedProperty(eventMap, "id");
        outStream.systemTime = BeanUtils.getNestedProperty(eventMap, "time");
        outStream.invocationType = BeanUtils.getNestedProperty(eventMap, "type");
        outStream.orgId = context.getOrg().get().getId(); // org.id
        outStream.orgDomainUrl = context.getOrg().get().getDomainUrl().toString(); // org.domainUrl

        String apiKey = BeanUtils.getNestedProperty(eventMap, "data.apiKey");
        if (apiKey != null) {
            outStream.fcAPIToken = apiKey;
        }
        String projectName = BeanUtils.getNestedProperty(eventMap, "data.projectName");
        if (projectName != null) {
            outStream.projectName = projectName;
        }
        String functionName = BeanUtils.getNestedProperty(eventMap, "data.functionName");
        if (functionName != null) {
            outStream.functionName = functionName;
        }
        String sourceState = BeanUtils.getNestedProperty(eventMap, "data.sourceState");
        if (sourceState != null) {
            outStream.sourceState = sourceState;
        }
        String statusMessage = BeanUtils.getNestedProperty(eventMap, "data.statusMessage");
        if (statusMessage != null) {
            outStream.statusMessage = statusMessage;
        }

        System.out.println("sop From FCLogger");
        logger.info(new Gson().toJson(outStream));
        logger.info("From FCLogger");
        this._fc_log_start_time();
    }

    public void _fc_log_start_time() {
        LocalDateTime myObj = LocalDateTime.now();
        this.startedAt = new Date();
        //outStream.startedAt = myObj.toString();
        //System.out.println(myObj);

        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String startedAt = myObj.format(myFormatObj);
        System.out.println("_fc_log_start_time: " + startedAt);
        outStream.startedAt = startedAt;
    }
    public void _fc_log_end_time() {
        this.fc_log_invocation_data(null);
    }
    public void fc_log_invocation_data(String LoggerMessage_from_apexsyslogjs) {
        LocalDateTime myObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String endedAt = myObj.format(myFormatObj);
        System.out.println("_fc_log_end_time: " + endedAt);

        Date endedNow = new Date();
        Long durationMS = endedNow.getTime() - this.startedAt.getTime();
        outStream.durationMS = durationMS;
        System.out.println("durationMS: " + durationMS);

        long billableDuration = durationMS == Math.floor(durationMS / 1000) ? durationMS : Double.valueOf(Math.floor(durationMS / 1000)).longValue() + 1;
        outStream.billingDurationSec = durationMS > 0 ? billableDuration : 1; // convert to sec on ceil

        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        outStream.memoryMB = (memoryAfter - memoryBefore) / 1024 / 1024;
        if (LoggerMessage_from_apexsyslogjs != null) {
            if(LoggerMessage_from_apexsyslogjs instanceof String) {
                outStream.sourceState = LoggerMessage_from_apexsyslogjs;
            } else {
                outStream.error = new Gson().toJson(LoggerMessage_from_apexsyslogjs);
            }
        }
        // if(outStream.fcAPIToken != null && outStream.fcAPIToken != "")
        logger.info(new Gson().toJson(outStream));
    }
    public void fc_log_post_message(Object mDataObj) {
        // let c_time = new Date().toISOString();
        //outStream.fc_log_post_message = new Gson().toJson(mDataObj);
    }
    public void _fc_log_memory_start() {
    }

    public void _fc_log_function_data() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Map<String, Object> eventMap = objectMapper.convertValue(event, Map.class);
        outStream.fc_log_function_data = new Gson().toJson(eventMap);
    }

    public void fc_log_function_data(Object e_data) {
        Date c_time = new Date();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Map<String, Object> eventMap = objectMapper.convertValue(event, Map.class);
        eventMap.put("c_time", c_time);
        eventMap.put("e_data", e_data);
        outStream.fc_log_function_data = new Gson().toJson(eventMap);
        /*Object nob = new Object();
        nob.c_time = c_time;
        nob.e_data = c_time;*/
        // {now: c_time, data: event.data};
    }

    public class LDObject {
        public String endedAt;
        public String startedAt;
        public String invocationId;
        public String systemTime;
        public String invocationType;
        public String fcLoggerVersion;
        public long memoryMB;
        public long durationMS;
        public long billingDurationSec;
        public String sourceState;
        public String statusMessage;
        public String functionName;
        public String projectName;
        public String orgId;
        public String orgDomainUrl;
        public String apiKey;
        public String fcAPIToken;
        public String fc_log_function_data;
        public String fc_log_post_message[];
        public String error;
        public void LDObject() {
        }
    }

}
