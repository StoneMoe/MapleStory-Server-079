package tools;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class FilePrinter {
    static void printError(final String cashShopDumpertxt, final Exception ex) {
        log.error(cashShopDumpertxt, ex);
    }
}
