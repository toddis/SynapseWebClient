package org.sagebionetworks.web.client.view;

import java.util.Date;
import java.util.List;

import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.FileDownload;
import org.sagebionetworks.web.shared.LicenseAgreement;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Defines the communication between the view and presenter for a view of a
 * single datasets.
 * 
 * @author jmhill
 * 
 */
public interface DatasetView extends IsWidget, SynapseView {

	/**
	 * This how the view communicates with the presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Send the view the details of the dataset to render.
	 * 
	 * @param id
	 * @param name
	 * @param overviewText
	 * @param diseases
	 * @param species
	 * @param studySize
	 * @param tissueTumor
	 * @param tissueTypes
	 * @param referencePublicationDisplay
	 * @param referencePublicationUrl
	 * @param nOtherPublications
	 * @param viewOtherPublicationsUrl
	 * @param postedDate
	 * @param curationDate
	 * @param lastModifiedDate
	 * @param creator
	 * @param contributors
	 * @param nFollowers
	 * @param viewFollowersUrl
	 * @param downloadAvailability
	 * @param releaseNotesUrl
	 * @param status
	 * @param version
	 * @param nSamples
	 * @param nDownloads
	 * @param citation
	 * @param pubmedId
	 * @param isAdministrator
	 * @param canEdit
	 */
	public void setDatasetDetails(String id, String name, String overviewText,
			String[] diseases, String[] species, int studySize,
			String tissueTumor, String[] tissueTypes,
			String referencePublicationDisplay, String referencePublicationUrl,
			int nOtherPublications, String viewOtherPublicationsUrl,
			Date postedDate, Date curationDate, Date lastModifiedDate,
			String creator, String[] contributors, int nFollowers,
			String viewFollowersUrl, String downloadAvailability,
			String releaseNotesUrl, String status, String version,
			int nSamples, int nDownloads, String citation, Integer pubmedId,
			boolean isAdministrator, boolean canEdit);

	/**
	 * require the view to show the license agreement
	 * 
	 * @param requireLicense
	 */
	public void requireLicenseAcceptance(boolean requireLicense);

	/**
	 * the license agreement to be shown
	 * 
	 * @param agreement
	 */
	public void setLicenseAgreement(LicenseAgreement agreement);

	/**
	 * Set the list of files available via the whole dataset download
	 * 
	 * @param downloads
	 */
	public void setDatasetDownloads(List<FileDownload> downloads);

	/**
	 * Disables the downloading of files
	 * 
	 * @param disable
	 */
	public void disableLicensedDownloads(boolean disable);

	/**
	 * Removes the download button
	 */
	public void setDownloadUnavailable();

	/**
	 * Defines the communication with the presenter.
	 * 
	 */
	public interface Presenter extends SynapsePresenter {

		/**
		 * Refreshes the object on the page
		 */
		public void refresh();

		/**
		 * called when the user has accepted the license in the view
		 */
		public void licenseAccepted();

		/**
		 * Available for the view the change the current Place
		 * 
		 * @param place
		 */
		public void goTo(Place place);

		/**
		 * Determine if a download screen can be displayed to the user
		 * 
		 * @return true if the user should be shown the download screen
		 */
		public boolean downloadAttempted();

		/**
		 * Delete this dataset
		 */
		public void delete();
	}

}
