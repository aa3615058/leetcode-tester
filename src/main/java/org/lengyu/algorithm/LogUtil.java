package org.lengyu.algorithm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtil {
    public static final Logger DATA_CONSOLE_LOGGER = LogManager.getLogger("dataLogger");;
    public static final Logger INFO_CONSOLE_LOGGER = LogManager.getLogger("infoLogger");
    public static final Logger ERR_CONSOLE_LOGGER = LogManager.getLogger("errLogger");
    public static final String INITIAL_ERR_LOG = "Initialization error";
    public static final String TIMEOUT_ERR_LOG = "Time Out!";
    public static final String LAST_TESTCASES_ERR_LOG = "Last Test Cases: ";
}
