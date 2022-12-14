package br.com.mundo_organico.Mundo_Organico.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.mundo_organico.Mundo_Organico.models.Ordered_Items;
import br.com.mundo_organico.Mundo_Organico.models.Request;
import br.com.mundo_organico.Mundo_Organico.repositories.OrderedItemsDAO;
import br.com.mundo_organico.Mundo_Organico.repositories.RequestDAO;

@Service
public class RequestService {

	@Autowired
	private OrderedItemsDAO orderedItemsDAO;

	@Autowired
	private RequestDAO requestDAO;

	public void save(Request request) {
		requestDAO.save(request);
	}

	public List<Ordered_Items> listItems() {
		List<Ordered_Items> items = orderedItemsDAO.findAll();
		return items;
	}

	public List<Ordered_Items> listItemsByUser(Integer id) {
		return orderedItemsDAO.findItemsByUser(id);
	}

	public Request findById(Integer id) {
		Optional<Request> obj = requestDAO.findById(id);
		return obj.get();
	}

	public List<Request> listRequests(Integer id) {
		List<Request> lista = requestDAO.findByUserId(id);
		return lista;
	}

	public List<Ordered_Items> listItemsByRequest(Integer id) {
		return orderedItemsDAO.findItemsByRequest(id);
	}

}
