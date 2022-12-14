package br.com.mundo_organico.Mundo_Organico.services;

import br.com.mundo_organico.Mundo_Organico.exception.UserInvalid;
import br.com.mundo_organico.Mundo_Organico.exception.UserNonexistentException;
import br.com.mundo_organico.Mundo_Organico.models.User;
import br.com.mundo_organico.Mundo_Organico.repositories.UserDAO;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private JavaMailSender emailSender;

	@Autowired
	private EmailService emailService;

	// vai de 4 à 31 (o padrão do gensalt() é 10)
	private static final int complexidadeSenha = 10;

	// salvar usuário
	public User save(User user) {
		return userDAO.saveAndFlush(user);
	}

	public String criptografarPassword(User user) {
		return BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(complexidadeSenha));
	}

	// enviar email após cadastro
	public void emailSend(User user) {

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom("mundorganicoc@gmail.com");
			message.setTo(user.getEmail());
			message.setSubject("Confirmação de cadastro Mundo Orgânico");
			message.setText("Olá, " + user.getName() + "."
					+ "\n Parabéns! Sua conta foi confirmada e sua sacolinha está pronta para ser usada. "
					+ "Temos diversos produtos e esperamos que você aproveite tudo o que temos a oferecer."
					+ "\n\n Equipe Mundo Orgânico.");

			emailSender.send(message);

		} catch (MailException e) {
			e.printStackTrace();
		}

	}
	
    public void validSaveUser(User user, String passwordValid) throws UserInvalid {

        if (user.getName().trim().isEmpty() || user.getCellphone().trim().isEmpty() || user.getEmail().trim().isEmpty()
                || user.getPassword().trim().isEmpty()) {
            // red.addFlashAttribute(); mensagem
            throw new UserInvalid("Os campos obrigatórios não podem estar vazio.");
        }

        if (this.userDAO.existsByEmail(user.getEmail()) && !user.getPassword().equals(passwordValid)) {
          throw new UserInvalid("Email já cadastrado.");

        } else if (this.userDAO.existsByEmail(user.getEmail())) {
            throw new UserInvalid("Email já cadastrado.");

        } else if (!user.getPassword().equals(passwordValid)) {
            throw new UserInvalid("As senhas não coincidem.");
        }

    }

	// verificar o email e senha do usuário para login
	public User login(String email, String password) throws UserNonexistentException, UserInvalid {

		if (email.isBlank()) {
			throw new UserInvalid("Insira o email.");
		}

		if (password.isBlank()) {
			throw new UserInvalid("Insira a senha.");
		}

		Optional<User> user = userDAO.findByEmail(email);

		if (user.isPresent()) {
			// Se a senha estiver correta
			if (BCrypt.checkpw(password, user.get().getPassword())) {
				return user.get();
			} else {
				throw new UserNonexistentException("Email e/ou senha inválidos");
			}
		} else {
			throw new UserNonexistentException("Email e/ou senha inválidos");
		}

	}

	// pesquisar usuário por ID
	public User findById(Integer id) {
		Optional<User> obj = userDAO.findById(id);
		return obj.get();
	}
	
    public User searchByEmail(String email) {
        return userDAO.findByEmail1(email);
    }

    // pesquisar do usuario por código de verificação
    public User searchByCod(String cod) {
        return userDAO.findByCod(cod);
    }

	// atualizar dados do usuário
	public void updateData(User user) {
		User entity = findById(user.getId());
		entity.setName(user.getName());
		entity.setCpf(user.getCpf());
		entity.setCellphone(user.getCellphone());
		userDAO.save(entity);
	}

	// alterar senha do usuário
	public void alterPassword(User user, String password) {
		user.setCodVerificar(null);
		user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(complexidadeSenha)));
		userDAO.save(user);
	}
	
    // verificar se a senha do usuário e a senha de confirmação são iguais.
    public void validPassword(User user, String PasswordValid) throws UserInvalid {

        if (!user.getPassword().equals(PasswordValid)) {
            throw new UserInvalid("As senhas não coincidem.");
        }
    }
    
    public void validCod(User user, User userVerificar) throws UserInvalid {

        if(user != null) {
            if(!user.getCodVerificar().equals(userVerificar.getCodVerificar())) {
                throw new UserInvalid("Código inválido.");
            }
        }
        else {
            throw new UserInvalid("Código inválido.");
        }

    }

	// atualizar dados do usuário
	public void updateDataC(User user) {
		User entity = findById(user.getId());
		entity.setEmail(user.getEmail());
		entity.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(complexidadeSenha)));
		userDAO.save(entity);
	}

    public void requestAlterPassword(String email) throws UserNonexistentException {
        User user = userDAO.findByEmail1(email);

        if(user != null) {

            String verificador = RandomStringUtils.randomAlphanumeric(6);
            user.setCodVerificar(verificador);

            userDAO.save(user);

            emailService.sendRequestAlterPassword(email, verificador);

        }
        else {
            throw new UserNonexistentException("Email não encontrado");
        }

    }

}
