package pl.swaggerexample.model;

import io.swagger.annotations.ApiModel;

@ApiModel("Role, that user can have.")
public enum Role
{
	USER,
	MANAGER,
	DEVELOPER
}