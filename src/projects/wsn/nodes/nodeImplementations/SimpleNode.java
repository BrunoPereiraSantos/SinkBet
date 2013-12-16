package projects.wsn.nodes.nodeImplementations;

import projects.wsn.nodes.messages.WsnMsg;
import projects.wsn.nodes.timers.WsnMessageTimer;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

public class SimpleNode extends Node {

	// Armazenar o n� que sera usado para alcan�ar a Estacao-Base
	private Node proximoNoAteEstacaoBase;
	// Armazena o n�mero de sequencia da �ltima mensagem recebida
	private Integer sequenceNumber = 0;

	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message message = inbox.next();
			if (message instanceof WsnMsg) {
				Boolean encaminhar = Boolean.TRUE;
				WsnMsg wsnMessage = (WsnMsg) message;
				if (wsnMessage.forwardingHop.equals(this)) { // A mensagem
																// voltou. O no
																// deve
																// descarta-la
					encaminhar = Boolean.FALSE;
				} else if (wsnMessage.tipoMsg == 0) { // A mensagem � um flood.
														// Devemos atualizar a
														// rota
					if (proximoNoAteEstacaoBase == null) {
						proximoNoAteEstacaoBase = inbox.getSender();
						sequenceNumber = wsnMessage.sequenceID;
					} else if (sequenceNumber < wsnMessage.sequenceID) {
						// Recurso simples para evitar loop.
						// Exemplo: Noh A transmite em brodcast. Noh B recebe a
						// msg e retransmite em broadcast.
						// Consequentemente, noh A ir� receber a msg. Sem esse
						// condicional, noh A iria retransmitir novamente,
						// gerando um loop
						sequenceNumber = wsnMessage.sequenceID;
					} else {
						encaminhar = Boolean.FALSE;
					}
				}
				if (encaminhar) {
					// Devemos alterar o campo forwardingHop(da
					// mensagem) para armazenar o
					// noh que vai encaminhar a mensagem.
					wsnMessage.forwardingHop = this;
					broadcast(wsnMessage);
				}
			}
		}
	}
	
	@NodePopupMethod(menuText = "Construir Arvore de Roteamento")
	public void construirRoteamento() {
		this.proximoNoAteEstacaoBase = this;
		WsnMsg wsnMessage = new WsnMsg(1, this, null, this, 0);
		WsnMessageTimer timer = new WsnMessageTimer(wsnMessage);
		timer.startRelative(1, this);
	}

	@Override
	public void preStep() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void neighborhoodChange() {
		// TODO Auto-generated method stub

	}

	@Override
	public void postStep() {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub

	}

}
