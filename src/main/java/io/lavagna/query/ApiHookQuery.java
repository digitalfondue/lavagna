package io.lavagna.query;

import java.util.List;

import ch.digitalfondue.npjt.Query;
import ch.digitalfondue.npjt.QueryRepository;
import io.lavagna.model.ApiHook;

@QueryRepository
public interface ApiHookQuery {
	
	@Query("select API_HOOK_NAME, API_HOOK_SCRIPT, API_HOOK_CONFIGURATION from LA_API_HOOK")
	List<ApiHook> findAll();
}
