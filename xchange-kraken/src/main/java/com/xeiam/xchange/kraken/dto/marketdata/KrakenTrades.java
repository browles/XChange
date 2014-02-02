/**
 * Copyright (C) 2012 - 2014 Xeiam LLC http://xeiam.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.xeiam.xchange.kraken.dto.marketdata;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.xeiam.xchange.kraken.dto.marketdata.KrakenTrades.KrakenTradesDeserializer;

@JsonDeserialize(using = KrakenTradesDeserializer.class)
public class KrakenTrades {

  private final List<KrakenTrade> trades;
  private final long last;

  public KrakenTrades(List<KrakenTrade> trades, long last) {

    this.trades = trades;
    this.last = last;
  }

  public long getLast() {

    return last;
  }

  public List<KrakenTrade> getTrades() {

    return trades;
  }

  @Override
  public String toString() {

    return "KrakenTrades [trades=" + trades + ", last=" + last + "]";
  }

  static class KrakenTradesDeserializer extends JsonDeserializer<KrakenTrades> {

    @Override
    public KrakenTrades deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {

      List<KrakenTrade> krakenTrades = new ArrayList<KrakenTrade>();
      long last = 0;
      ObjectCodec oc = jsonParser.getCodec();
      JsonNode node = oc.readTree(jsonParser);
      Iterator<Entry<String, JsonNode>> tradesResultIterator = node.fields();
      while (tradesResultIterator.hasNext()) {
        Entry<String, JsonNode> entry = tradesResultIterator.next();
        String key = entry.getKey();
        JsonNode value = entry.getValue();
        if (key == "last") {
          last = value.asLong();
        }
        else if (value.isArray()) {
          for (JsonNode tradeJsonNode : value) {
            BigDecimal price = new BigDecimal(tradeJsonNode.path(0).asText());
            BigDecimal volume = new BigDecimal(tradeJsonNode.path(1).asText());
            double time = tradeJsonNode.path(2).asDouble();
            String type = tradeJsonNode.path(3).asText();
            String orderType = tradeJsonNode.path(4).asText();
            String miscellaneous = tradeJsonNode.path(5).asText();

            krakenTrades.add(new KrakenTrade(price, volume, time, type, orderType, miscellaneous));
          }
        }
      }
      return new KrakenTrades(krakenTrades, last);
    }

  }
}
