package org.example;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

public class Main {
  public interface Test {
    void run(List<List<CharSequence>> arg);
  }

  public static void main(String[] args) {
    String modulePath = Paths.get("js/index.js").toAbsolutePath().toString();

    var options = new HashMap<String, String>();
    options.put("js.commonjs-require", "true");
    options.put("js.commonjs-require-cwd", modulePath);

    var ctx =
        Context.newBuilder("js")
            .allowExperimentalOptions(true)
            .allowHostAccess(HostAccess.ALL)
            .allowIO(true)
            .options(options)
            .build();

    Value global = ctx.getBindings("js").getMember("globalThis");

    global.putMember("FirstTest", (Test) System.out::println);
    global.putMember("SecondTest", (Test) System.out::println);

    var module = ctx.eval("js", "require('%s')".formatted(modulePath));

    List.of("runTest1", "runTest2").forEach(name -> {
      var jsFunc = module.getMember(name);
      if (jsFunc == null) {
        throw new RuntimeException("can't find js func");
      }

      jsFunc.executeVoid();
    });

  }
}