package io.rootstock;

import io.rootstock.config.RskSystemProperties;
import io.rootstock.core.RootStock;
import io.rootstock.core.RootStockFactory;
import org.ethereum.cli.CLIInterface;
import io.rootstock.mine.TxBuilder;
import org.ethereum.peg.BtcLockClient;
import org.ethereum.peg.BtcReleaseClient;
import org.ethereum.rpc.JsonRpcListener;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * Created by ajlopez on 3/3/2016.
 */
public class Start {
    public static void main(String args[]) throws Exception {
        CLIInterface.call(args);

        if (!CONFIG.blocksLoader().equals("")) {
            CONFIG.setSyncEnabled(false);
            CONFIG.setDiscoveryEnabled(false);
        }

        RootStock rootstock = RootStockFactory.createRootStock();

        if (!CONFIG.blocksLoader().equals(""))
            rootstock.getBlockLoader().loadBlocks();

        rootstock.getMinerServer().start();

        if (RskSystemProperties.RSKCONFIG.minerEnable()) {
            rootstock.getMinerClient().mine();
        }

        // TODO adding transaction simulation
        if (CONFIG.simulateTxs()) {
            new TxBuilder().simulateTxs(rootstock);
        }


        if (CONFIG.isFederatorEnabled()) {
            BtcLockClient btcLockClient = (BtcLockClient) RootStockFactory.context.getBean("btcLockClient");
            btcLockClient.setup();

            final BtcReleaseClient btcReleaseClient = (BtcReleaseClient) RootStockFactory.context.getBean("btcReleaseClient");
            new Thread(){
                @Override
                public void run() {
                    try {
                        btcReleaseClient.setup();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        }

        // TODO adding rpc
        if (CONFIG.isRpcEnabled()) {
            new JsonRpcListener(rootstock).start();
        }
    }
	
	public void desdemaquina() {}
}
