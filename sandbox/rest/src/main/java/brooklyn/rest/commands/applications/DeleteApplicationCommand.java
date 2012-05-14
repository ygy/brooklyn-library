package brooklyn.rest.commands.applications;

import brooklyn.rest.commands.BrooklynCommand;
import static com.google.common.base.Preconditions.checkArgument;
import com.sun.jersey.api.client.ClientResponse;
import com.yammer.dropwizard.client.JerseyClient;
import com.yammer.dropwizard.json.Json;
import org.apache.commons.cli.CommandLine;

public class DeleteApplicationCommand extends BrooklynCommand {

  public DeleteApplicationCommand() {
    super("delete-application", "Delete application by name");
  }

  @Override
  public String getSyntax() {
    return "[options] <application name>";
  }

  @Override
  protected void run(Json json, JerseyClient client, CommandLine params) throws Exception {
    checkArgument(params.getArgList().size() >= 1, "Application name is mandatory");

    String name = (String) params.getArgList().get(0);
    ClientResponse response = client.delete(uriFor("/v1/applications/" + name),
        ClientResponse.class);

    System.out.println("Ok, status: " + response.getStatus());
  }
}
